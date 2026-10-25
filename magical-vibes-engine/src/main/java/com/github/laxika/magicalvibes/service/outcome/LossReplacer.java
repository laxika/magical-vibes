package com.github.laxika.magicalvibes.service.outcome;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingLossReplacement;
import com.github.laxika.magicalvibes.service.effect.normalfx.LifeSupport;

import java.util.UUID;

/**
 * A replacement effect for the game-loss event (CR 614 applied to CR 104.3).
 *
 * <p>Every {@code @Component} implementing this is consulted by
 * {@code GameOutcomeService.resolveLoss} before a player is allowed to lose, so a new card of
 * this kind is registered by existing as a bean — no loss call site has to learn about it.
 * Implementations run <em>after</em> the "can't lose" checks: a loss that never happens is not a
 * loss to replace ("If you can't lose the game (for example, you control a Platinum Angel),
 * Lich's Mirror won't do anything").
 */
public interface LossReplacer {

    /**
     * Replaces {@code losingPlayerId}'s loss if this replacer applies to them and to
     * {@code reason}.
     *
     * <p>An implementation that returns {@code true} owns everything the replacement entails:
     * mutating game state and appending its own game log. Returning {@code false} must leave the
     * game untouched so the next replacer — or the loss itself — proceeds.
     *
     * @return {@code true} if the loss was replaced and the player no longer loses
     */
    boolean tryReplace(GameData gameData, UUID losingPlayerId, LossReason reason);

    /**
     * Asks, before a replacement changes anything, the questions of the Lich's Duel Mastery sacrifice that setting
     * {@code losingPlayerId}'s life total to {@code newLife} would cause
     * ({@link LifeSupport#askBeforeLossReplacementLifeTotal}). Outside a rerun window the loss then waits in
     * {@link GameData#pendingLossReplacement} and is resolved again once the answers are in
     * ({@code GameOutcomeService.resumeLossReplacement}), when the replacement runs again and the life change's
     * sacrifice consumes them.
     *
     * @return {@code true} when a question is open: the replacement must return {@code true} and change nothing
     */
    static boolean askBeforeLifeTotal(LifeSupport lifeSupport, GameData gameData, UUID losingPlayerId,
                                      LossReason reason, int newLife) {
        if (!lifeSupport.askBeforeLossReplacementLifeTotal(gameData, losingPlayerId, newLife)) {
            if (gameData.pendingLossReplacement != null
                    && losingPlayerId.equals(gameData.pendingLossReplacement.playerId())) {
                gameData.pendingLossReplacement = null;
            }
            return false;
        }
        gameData.pendingLossReplacement = new PendingLossReplacement(losingPlayerId, reason.name());
        return true;
    }
}
