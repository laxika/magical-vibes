package com.github.laxika.magicalvibes.model.effect;

/** Makes a creature unable to be blocked for the current turn. */
public record MakeCreatureUnblockableEffect(boolean selfTargeting, boolean attachedPermanent)
        implements CardEffect {

    public MakeCreatureUnblockableEffect() {
        this(false, false);
    }

    public MakeCreatureUnblockableEffect(boolean selfTargeting) {
        this(selfTargeting, false);
    }

    /** Makes the permanent attached to the source Equipment or Aura unblockable this turn. */
    public static MakeCreatureUnblockableEffect forAttachedPermanent() {
        return new MakeCreatureUnblockableEffect(false, true);
    }

    @Override
    public TargetSpec targetSpec() {
        return selfTargeting || attachedPermanent
                ? new TargetSpec(null, false, null, true, 1)
                : TargetSpec.benign(TargetPredicates.permanent());
    }

    @Override
    public boolean resolvesAgainstAttachedPermanent() {
        return attachedPermanent;
    }
}
