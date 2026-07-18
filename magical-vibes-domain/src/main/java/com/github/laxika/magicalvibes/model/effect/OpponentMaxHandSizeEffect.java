package com.github.laxika.magicalvibes.model.effect;

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
}
