package com.github.laxika.magicalvibes.model.effect;

/** Makes a creature unable to be blocked for the specified duration. */
public record MakeCreatureUnblockableEffect(boolean selfTargeting, boolean attachedPermanent,
                                            EffectDuration duration)
        implements CardEffect {

    public MakeCreatureUnblockableEffect(boolean selfTargeting, boolean attachedPermanent) {
        this(selfTargeting, attachedPermanent, EffectDuration.UNTIL_END_OF_TURN);
    }

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
                : TargetSpec.benign(TargetPredicates.creature());
    }

    @Override
    public boolean resolvesAgainstAttachedPermanent() {
        return attachedPermanent;
    }
}
