package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/** Makes a creature unable to be blocked for the specified duration. */
public record MakeCreatureUnblockableEffect(boolean selfTargeting, boolean attachedPermanent,
                                            EffectDuration duration, PermanentPredicate filter)
        implements CardEffect {

    public MakeCreatureUnblockableEffect(boolean selfTargeting, boolean attachedPermanent) {
        this(selfTargeting, attachedPermanent, EffectDuration.UNTIL_END_OF_TURN, null);
    }

    public MakeCreatureUnblockableEffect() {
        this(false, false);
    }

    public MakeCreatureUnblockableEffect(boolean selfTargeting) {
        this(selfTargeting, false);
    }

    public MakeCreatureUnblockableEffect(boolean selfTargeting, boolean attachedPermanent,
                                         EffectDuration duration) {
        this(selfTargeting, attachedPermanent, duration, null);
    }

    /** Makes a targeted creature unblockable, optionally narrowed by a permanent predicate. */
    public MakeCreatureUnblockableEffect(PermanentPredicate filter) {
        this(false, false, EffectDuration.UNTIL_END_OF_TURN, filter);
    }

    /** Makes the permanent attached to the source Equipment or Aura unblockable this turn. */
    public static MakeCreatureUnblockableEffect forAttachedPermanent() {
        return new MakeCreatureUnblockableEffect(false, true);
    }

    @Override
    public TargetSpec targetSpec() {
        return selfTargeting || attachedPermanent
                ? new TargetSpec(null, false, null, true, 1)
                : TargetSpec.benign(TargetPredicates.creature(), filter);
    }

    @Override
    public boolean resolvesAgainstAttachedPermanent() {
        return attachedPermanent;
    }
}
