package com.proarena.tournament;

import com.proarena.user.User;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name="tournaments")
public class Tournament {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(nullable=false,length=120) private String name;
    @Column(nullable=false,length=80) private String game;
    @Column(nullable=false) private LocalDate startDate;
    private LocalDate endDate;
    @Column(nullable=false) private Integer maxParticipants;
    @Column(precision=12,scale=2) private BigDecimal prizePool;
    @Column(length=4000) private String rules;
    @Column(length=80) private String platform;
    @Column(precision=12,scale=2) private BigDecimal registrationFee;
    @Column(nullable=false,length=40) private String tournamentType;
    @Column(length=500) private String streamUrl;
    @Column(length=500) private String bannerImageUrl;
    @ManyToOne(optional=false,fetch=FetchType.LAZY) @JoinColumn(name="organizer_id",nullable=false) private User organizer;
    @Column(nullable=false,updatable=false) private Instant createdAt;

    protected Tournament() {}
    public Tournament(String name,String game,LocalDate startDate,LocalDate endDate,Integer maxParticipants,
                      BigDecimal prizePool,String rules,String platform,BigDecimal registrationFee,
                      String tournamentType,String streamUrl,String bannerImageUrl,User organizer) {
        this.name=name; this.game=game; this.startDate=startDate; this.endDate=endDate;
        this.maxParticipants=maxParticipants; this.prizePool=prizePool; this.rules=rules;
        this.platform=platform; this.registrationFee=registrationFee; this.tournamentType=tournamentType;
        this.streamUrl=streamUrl; this.bannerImageUrl=bannerImageUrl; this.organizer=organizer;
        this.createdAt=Instant.now();
    }
    public Long getId(){return id;} public String getName(){return name;} public String getGame(){return game;}
    public LocalDate getStartDate(){return startDate;} public LocalDate getEndDate(){return endDate;}
    public Integer getMaxParticipants(){return maxParticipants;} public BigDecimal getPrizePool(){return prizePool;}
    public String getRules(){return rules;} public String getPlatform(){return platform;}
    public BigDecimal getRegistrationFee(){return registrationFee;} public String getTournamentType(){return tournamentType;}
    public String getStreamUrl(){return streamUrl;} public String getBannerImageUrl(){return bannerImageUrl;}
    public String getOrganizerUsername(){return organizer.getUsername();} public Instant getCreatedAt(){return createdAt;}
}