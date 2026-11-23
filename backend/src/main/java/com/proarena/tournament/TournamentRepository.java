package com.proarena.tournament;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TournamentRepository extends JpaRepository<Tournament,Long> {
    List<Tournament> findAllByOrderByStartDateAsc();
    List<Tournament> findByOrganizer_EmailIgnoreCaseOrderByCreatedAtDesc(String email);
}