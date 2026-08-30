package com.github.laxika.magicalvibes.model.effect;

/**
 * The target player discards a fixed number of cards, then the controller draws cards equal to
 * the number the target was unable to discard.
 *
 * @param amount number of cards the target player must discard
 */
public record TargetPlayerDiscardsThenControllerDrawsShortfallEffect(int amount)
        implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.player());
    }
}
