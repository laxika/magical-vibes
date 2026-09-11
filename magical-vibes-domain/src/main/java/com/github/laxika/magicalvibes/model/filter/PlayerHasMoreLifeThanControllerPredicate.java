package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches an opponent whose life total is greater than the evaluating controller's life total.
 * The life comparison is always checked when selecting the target. Cards such as Oath of Mages
 * also require it at resolution; Keeper abilities check it only as they are activated.
 */
public record PlayerHasMoreLifeThanControllerPredicate(boolean recheckAtResolution) implements PlayerPredicate {
    public PlayerHasMoreLifeThanControllerPredicate() {
        this(false);
    }
}
