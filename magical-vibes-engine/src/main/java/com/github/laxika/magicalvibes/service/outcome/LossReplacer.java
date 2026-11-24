package com.github.laxika.magicalvibes.service.outcome;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingLossReplacement;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.effect.normalfx.LifeSupport;

import java.util.List;
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
 *
 * <p>A loss replacement belongs to a permanent ({@link #sources}). The sources of every replacer are
 * gathered first and the losing player chooses the one that applies when there are several (CR 616.1);
 * {@link #replace} then applies that one.
 */
public interface LossReplacer {

    /**
     * A replacement that applies before the loss is judged and is not one of the loss replacements the
     * player chooses among (Enduring Angel's replacement of a life total reduced to 0 or less).
     *
     * @return {@code true} if it applied and the player no longer loses
     */
    default boolean replacesBeforeLoss(GameData gameData, UUID losingPlayerId, LossReason reason) {
        return false;
    }

    /**
     * The permanents whose replacement of this kind would replace {@code losingPlayerId}'s loss for
     * {@code reason}, judged without changing the game.
     */
    default List<Permanent> sources(GameData gameData, UUID losingPlayerId, LossReason reason) {
        return List.of();
    }

    /**
     * Replaces {@code losingPlayerId}'s loss with {@code source}'s replacement, one of {@link #sources}.
     *
     * <p>An implementation that returns {@code true} owns everything the replacement entails:
     * mutating game state and appending its own game log. Returning {@code false} must leave the
     * game untouched so another replacement — or the loss itself — proceeds.
     *
     * @return {@code true} if the loss was replaced (or waits for a question) and the player no longer loses
     */
    default boolean replace(GameData gameData, UUID losingPlayerId, Permanent source, LossReason reason) {
        return false;
    }

    /**
     * Asks, before a replacement changes anything, the questions of the Lich's Duel Mastery sacrifice that setting
     * {@code losingPlayerId}'s life total to {@code newLife} would cause
     * ({@link LifeSupport#askBeforeLossReplacementLifeTotal}). Outside a rerun window the loss then waits in
     * {@link GameData#pendingLossReplacement}, with the replacing {@code source} it will apply, and is resolved again
     * once the answers are in ({@code GameOutcomeService.resumeLossReplacement}), when that replacement runs again
     * and the life change's sacrifice consumes them.
     *
     * @return {@code true} when a question is open: the replacement must return {@code true} and change nothing
     */
    static boolean askBeforeLifeTotal(LifeSupport lifeSupport, GameData gameData, UUID losingPlayerId,
                                      LossReason reason, int newLife, Permanent source) {
        if (!lifeSupport.askBeforeLossReplacementLifeTotal(gameData, losingPlayerId, newLife)) {
            if (gameData.pendingLossReplacement != null
                    && losingPlayerId.equals(gameData.pendingLossReplacement.playerId())) {
                gameData.pendingLossReplacement = null;
            }
            return false;
        }
        gameData.pendingLossReplacement = new PendingLossReplacement(losingPlayerId, reason.name(), source.getId());
        return true;
    }
}
