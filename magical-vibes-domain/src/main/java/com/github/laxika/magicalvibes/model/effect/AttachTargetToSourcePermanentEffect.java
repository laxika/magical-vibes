package com.github.laxika.magicalvibes.model.effect;

/**
 * Attaches the targeted permanent to the source permanent.
 * Used for effects that steal equipment and attach it to the attacking creature
 * (e.g. Ogre Geargrabber). Typically combined with
 * {@link GainControlOfTargetEffect} with {@link ControlDuration#END_OF_TURN}.
 */
public record AttachTargetToSourcePermanentEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetCategory.PERMANENT);
    }
}
