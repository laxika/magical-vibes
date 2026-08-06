package com.github.laxika.magicalvibes.model.effect;

/**
 * Capability interface for static effects that change their own controller's maximum hand size.
 *
 * <p>The controller-scoped counterpart of {@link OpponentMaxHandSizeEffect}.
 * {@code TurnCleanupService.getMaxHandSize} folds every hand-size effect over the running value in
 * timestamp order (CR 402.2), so a controller-scoped "set to a specific value"
 * ({@link SetControllerMaximumHandSizeEffect}) combines correctly with opponent-controlled
 * reductions. A "no maximum hand size" effect still wins and is handled separately in
 * {@code TurnCleanupService.hasNoMaximumHandSize}.
 */
public interface ControllerMaxHandSizeEffect extends CardEffect {

    /**
     * @param currentMax the controller's running maximum hand size before this effect applies
     * @return the maximum hand size after applying this effect
     */
    int applyToMaximumHandSize(int currentMax);
}
