package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.model.DraftData;

import java.util.UUID;

/**
 * Engine-side seam for notifying the draft/tournament layer that a tournament game finished.
 * The engine ({@code GameOutcomeService}) depends only on this interface; the application layer
 * provides the implementation, keeping draft orchestration out of the game engine module.
 */
public interface TournamentResultHandler {

    void handleGameFinished(DraftData draftData, UUID winnerId);
}
