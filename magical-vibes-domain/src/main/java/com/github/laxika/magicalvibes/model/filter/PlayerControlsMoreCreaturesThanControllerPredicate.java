package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches an opponent who controls strictly more creatures than the evaluating controller.
 * The comparison is always checked when the target is chosen. When {@code recheckAtResolution}
 * is true the comparison is applied again on resolution; cards such as Keeper of the Beasts
 * check it only as the ability is activated and pass {@code false}.
 */
public record PlayerControlsMoreCreaturesThanControllerPredicate(boolean recheckAtResolution)
        implements PlayerPredicate {

    public PlayerControlsMoreCreaturesThanControllerPredicate() {
        this(true);
    }
}
