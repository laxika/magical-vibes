package com.github.laxika.magicalvibes.service.outcome;

import com.github.laxika.magicalvibes.model.GameData;

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
}
