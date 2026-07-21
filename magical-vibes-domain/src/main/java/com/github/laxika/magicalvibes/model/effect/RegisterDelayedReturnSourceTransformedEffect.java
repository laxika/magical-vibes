package com.github.laxika.magicalvibes.model.effect;

/**
 * Registers a delayed trigger to return the source card from its owner's graveyard
 * to the battlefield transformed at the beginning of the next end step.
 *
 * @param onlyIfSacrificed  when true, only triggers if the permanent was sacrificed
 *                          ("When you sacrifice this…"), not on other deaths
 * @param underOwnerControl when true, the permanent returns under its owner's control;
 *                          otherwise under the trigger controller's control ("under your control")
 */
public record RegisterDelayedReturnSourceTransformedEffect(
        boolean onlyIfSacrificed,
        boolean underOwnerControl
) implements CardEffect {

    /** Loyal Cathar-style: triggers on any death, returns under your (controller's) control. */
    public RegisterDelayedReturnSourceTransformedEffect() {
        this(false, false);
    }

    @Override
    public boolean onlyTriggersOnSacrifice() {
        return onlyIfSacrificed;
    }
}
