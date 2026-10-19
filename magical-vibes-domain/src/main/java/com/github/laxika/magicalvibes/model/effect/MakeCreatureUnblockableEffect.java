package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Makes a creature unable to be blocked for the specified duration. When
 * {@code notBlockableByOpponentsOfController} is set, the creature instead can't be blocked by
 * creatures the resolving ability's controller's opponents control ("can't be blocked by creatures
 * your opponents control").
 */
public record MakeCreatureUnblockableEffect(boolean selfTargeting, boolean attachedPermanent,
                                            EffectDuration duration, PermanentPredicate filter,
                                            boolean triggeringPermanent, boolean targetAnyPermanent,
                                            boolean notBlockableByOpponentsOfController)
        implements CardEffect {

    public MakeCreatureUnblockableEffect(boolean selfTargeting, boolean attachedPermanent,
                                         EffectDuration duration, PermanentPredicate filter,
                                         boolean triggeringPermanent, boolean targetAnyPermanent) {
        this(selfTargeting, attachedPermanent, duration, filter, triggeringPermanent, targetAnyPermanent, false);
    }

    public MakeCreatureUnblockableEffect(boolean selfTargeting, boolean attachedPermanent,
                                         EffectDuration duration, PermanentPredicate filter,
                                         boolean triggeringPermanent) {
        this(selfTargeting, attachedPermanent, duration, filter, triggeringPermanent, false);
    }

    public MakeCreatureUnblockableEffect(boolean selfTargeting, boolean attachedPermanent,
                                         EffectDuration duration, PermanentPredicate filter) {
        this(selfTargeting, attachedPermanent, duration, filter, false);
    }

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

    /** Applies to the permanent whose action caused the trigger without choosing a target. */
    public static MakeCreatureUnblockableEffect forTriggeringPermanent() {
        return new MakeCreatureUnblockableEffect(false, false, EffectDuration.UNTIL_END_OF_TURN, null, true);
    }

    /**
     * Applies to the permanent whose action caused the trigger without choosing a target; it can't be
     * blocked by creatures the ability's controller's opponents control this turn.
     */
    public static MakeCreatureUnblockableEffect forTriggeringPermanentNotBlockableByOpponents() {
        return new MakeCreatureUnblockableEffect(false, false, EffectDuration.UNTIL_END_OF_TURN, null,
                true, false, true);
    }

    /** Allows subtype-based wording to target noncreature permanents as well. */
    public static MakeCreatureUnblockableEffect forTargetPermanent(PermanentPredicate filter) {
        return new MakeCreatureUnblockableEffect(false, false, EffectDuration.UNTIL_END_OF_TURN,
                filter, false, true);
    }

    @Override
    public TargetSpec targetSpec() {
        return selfTargeting || attachedPermanent || triggeringPermanent
                ? new TargetSpec(null, false, null, true, 1)
                : TargetSpec.benign(targetAnyPermanent ? TargetPredicates.permanent()
                : TargetPredicates.creature(), filter);
    }

    @Override
    public boolean resolvesAgainstAttachedPermanent() {
        return attachedPermanent;
    }
}
