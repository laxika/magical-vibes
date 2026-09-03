package com.github.laxika.magicalvibes.model.effect;

/**
 * Attaches the source Fortification to a target land.
 *
 * <p>The source is expected to be a Fortification permanent on the battlefield. The target is
 * checked again when the effect resolves because the target may have stopped being a land.</p>
 */
public record AttachSourceFortificationToTargetLandEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.land());
    }
}
