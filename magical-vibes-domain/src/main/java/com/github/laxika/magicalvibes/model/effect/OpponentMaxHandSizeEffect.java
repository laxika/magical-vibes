package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.GameData;

import java.util.UUID;

/**
 * Capability interface for static effects, controlled by a player, that change an
 * opponent's maximum hand size.
 *
 * <p>{@code TurnCleanupService.getMaxHandSize} folds over the opponent's permanents in
 * timestamp order (CR 402.2), applying each such effect to the running value. That lets a
 * "reduce by N" effect ({@link ReduceOpponentMaxHandSizeEffect}) and a "set to a specific
 * value" effect ({@link SetOpponentMaximumHandSizeEffect}) combine correctly. A "no maximum
 * hand size" effect still wins and is handled separately in
 * {@code TurnCleanupService.hasNoMaximumHandSize}.
 */
public interface OpponentMaxHandSizeEffect extends CardEffect {

    /**
     * @param currentMax the opponent's running maximum hand size before this effect applies
     * @return the maximum hand size after applying this effect
     */
    int applyToMaximumHandSize(int currentMax);

    /**
     * Applies this effect when the source controller and current game state are available.
     * Fixed-value effects use the default implementation.
     */
    default int applyToMaximumHandSize(int currentMax, GameData gameData, UUID sourceControllerId) {
        return applyToMaximumHandSize(currentMax);
    }
}
