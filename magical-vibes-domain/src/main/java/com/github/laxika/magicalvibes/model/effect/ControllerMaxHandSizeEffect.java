package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Permanent;

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

    /**
     * Applies this effect when the source permanent's current state is available.
     *
     * <p>Fixed-value effects use the default implementation. Effects whose value depends on the
     * source permanent can override this overload.
     */
    default int applyToMaximumHandSize(int currentMax, Permanent source) {
        return applyToMaximumHandSize(currentMax);
    }
}
