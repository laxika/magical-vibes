package com.github.laxika.magicalvibes.webservice;

import com.github.laxika.magicalvibes.model.DraftData;
import com.github.laxika.magicalvibes.service.TournamentResultHandler;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Application-layer implementation of the engine's {@link TournamentResultHandler} seam:
 * forwards finished tournament games to {@link DraftService} so the bracket can advance.
 */
@Component
public class DraftTournamentResultHandler implements TournamentResultHandler {

    private final DraftService draftService;

    public DraftTournamentResultHandler(@Lazy DraftService draftService) {
        this.draftService = draftService;
    }

    @Override
    public void handleGameFinished(DraftData draftData, UUID winnerId) {
        draftService.handleGameFinished(draftData, winnerId);
    }
}
