package com.proarena.tournament;

import com.proarena.user.UserRepository;
import jakarta.validation.constraints.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/tournaments")
public class TournamentController {
    private final TournamentRepository tournaments;
    private final UserRepository users;

    public TournamentController(TournamentRepository tournaments, UserRepository users) {
        this.tournaments=tournaments; this.users=users;
    }

    public record TournamentView(Long id,String name,String game,LocalDate startDate,LocalDate endDate,
            Integer maxParticipants,BigDecimal prizePool,String rules,String platform,BigDecimal registrationFee,
            String tournamentType,String streamUrl,String bannerImageUrl,String organizer,java.time.Instant createdAt) {
        static TournamentView from(Tournament t) {
            return new TournamentView(t.getId(),t.getName(),t.getGame(),t.getStartDate(),t.getEndDate(),
                    t.getMaxParticipants(),t.getPrizePool(),t.getRules(),t.getPlatform(),t.getRegistrationFee(),
                    t.getTournamentType(),t.getStreamUrl(),t.getBannerImageUrl(),t.getOrganizerUsername(),t.getCreatedAt());
        }
    }

    @GetMapping
    public List<TournamentView> list() {
        return tournaments.findAllByOrderByStartDateAsc().stream().map(TournamentView::from).toList();
    }

    @GetMapping("/{id}")
    public TournamentView get(@PathVariable Long id) {
        return tournaments.findById(id).map(TournamentView::from)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Tournament not found"));
    }

    @GetMapping("/mine")
    public List<TournamentView> mine(Authentication auth) {
        return tournaments.findByOrganizer_EmailIgnoreCaseOrderByCreatedAtDesc(auth.getName())
                .stream().map(TournamentView::from).toList();
    }

    @PostMapping(consumes={"multipart/form-data"})
    @ResponseStatus(HttpStatus.CREATED)
    public TournamentView create(@RequestParam @NotBlank @Size(max=120) String name,
            @RequestParam @NotBlank @Size(max=80) String game,
            @RequestParam @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam @Min(2) @Max(100000) Integer maxParticipants,
            @RequestParam(required=false) @DecimalMin("0.0") BigDecimal prizePool,
            @RequestParam(required=false) String rules, @RequestParam(required=false) String platform,
            @RequestParam(required=false) @DecimalMin("0.0") BigDecimal registrationFee,
            @RequestParam(defaultValue="single-elimination") String tournamentType,
            @RequestParam(required=false) String streamUrl,
            @RequestParam(required=false) MultipartFile bannerImage, Authentication auth) {
        if (endDate != null && endDate.isBefore(startDate))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"End date must be on or after start date");
        String bannerUrl = storeImage(bannerImage);
        var organizer = users.findByEmailIgnoreCase(auth.getName()).orElseThrow();
        Tournament saved = tournaments.save(new Tournament(name.trim(),game.trim(),startDate,endDate,maxParticipants,
                zero(prizePool),rules,platform,zero(registrationFee),tournamentType,streamUrl,bannerUrl,organizer));
        return TournamentView.from(saved);
    }

    private BigDecimal zero(BigDecimal value) { return value == null ? BigDecimal.ZERO : value; }

    private String storeImage(MultipartFile file) {
        if (file == null || file.isEmpty()) return null;
        if (file.getSize() > 5 * 1024 * 1024) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Banner image must be 5 MB or smaller");
        String type=file.getContentType();
        if (!List.of("image/jpeg","image/png","image/webp","image/gif").contains(type))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Banner must be a JPEG, PNG, WebP or GIF image");
        String extension=switch(type) { case "image/jpeg" -> ".jpg"; case "image/png" -> ".png"; case "image/webp" -> ".webp"; default -> ".gif"; };
        try {
            Path directory=Path.of("uploads").toAbsolutePath().normalize();
            Files.createDirectories(directory);
            String filename=java.util.UUID.randomUUID()+extension;
            Files.copy(file.getInputStream(),directory.resolve(filename),StandardCopyOption.REPLACE_EXISTING);
            return "/uploads/"+filename;
        } catch(IOException e) { throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,"Could not save tournament banner"); }
    }
}