package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.ChosenOpponentFollowUp;
import com.github.laxika.magicalvibes.model.GameData;

import java.util.UUID;

/**
 * Goes on with a resolution once the opponent its "an opponent" instruction names is known
 * ({@link ChosenOpponentSupport#chooseThen}). One bean per {@link ChosenOpponentFollowUp} record.
 */
public interface ChosenOpponentContinuation<F extends ChosenOpponentFollowUp> {

    Class<F> followUpType();

    /** Continues {@code followUp}'s flow with {@code opponentId}, {@code null} when the controller has no opponent. */
    void continueWithOpponent(GameData gameData, UUID opponentId, F followUp);
}
