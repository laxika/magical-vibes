package com.github.laxika.magicalvibes.model.effect;

/**
 * "Sacrifice this permanent. If you do, {@code thenEffect}." Sacrifices the stack entry's source
 * permanent and resolves {@code thenEffect} only when the sacrifice actually happened — the
 * contingency a bare list of effects (or a {@link SequenceEffect}, which explicitly has none)
 * cannot express. A source that has already left the battlefield can't be sacrificed, so nothing
 * else happens either.
 *
 * <p>Pair with a {@link MayEffect} wrapper for the common "you may sacrifice it. If you do, …"
 * template (Impaler Shrike, Mindstab Thrull): the may/sacrifice choice waits for resolution
 * (CR 603.5), while any target the payload declares is chosen as the trigger goes on the stack
 * (CR 603.3d).
 *
 * @param thenEffect the payload resolved after a successful sacrifice
 * @param reflexive whether the payload is put on the stack as a separate reflexive ability
 * @param sacrificesTriggeringPermanent whether "it" is the permanent the trigger event was about (the stack entry's
 *                                      triggering permanent) rather than the source, whenever the entry records one
 */
public record SacrificeSelfThenEffect(CardEffect thenEffect, boolean reflexive, boolean sacrificesTriggeringPermanent)
        implements CardEffect {

    public SacrificeSelfThenEffect(CardEffect thenEffect) {
        this(thenEffect, false);
    }

    public SacrificeSelfThenEffect(CardEffect thenEffect, boolean reflexive) {
        this(thenEffect, reflexive, false);
    }

    public static SacrificeSelfThenEffect reflexive(CardEffect thenEffect) {
        return new SacrificeSelfThenEffect(thenEffect, true);
    }

    /**
     * "Whenever this creature or equipped creature deals combat damage to a player, you may sacrifice it. If you do, …"
     * (Ogre-Head Helm): sacrifices the creature that dealt the damage, the entry's triggering permanent when the
     * Equipment's ability recorded one, else the source itself.
     */
    public static SacrificeSelfThenEffect ofTriggeringPermanent(CardEffect thenEffect) {
        return new SacrificeSelfThenEffect(thenEffect, false, true);
    }

    public SacrificeSelfThenEffect {
        if (thenEffect == null) {
            throw new IllegalArgumentException("SacrificeSelfThenEffect requires a payload; use SacrificeSelfEffect for a bare sacrifice");
        }
    }

    /** The payload supplies chosen targets; the wrapper also needs its source bound for sacrifice. */
    @Override
    public TargetSpec targetSpec() {
        if (reflexive) {
            return TargetSpec.NONE;
        }
        TargetSpec payload = thenEffect.targetSpec();
        return new TargetSpec(payload.declaredTarget(), payload.harmful(), payload.predicate(), true,
                payload.playerTargetCount());
    }

    /** "When you do": a reflexive payload's targets are its own ability's, chosen as that ability triggers. */
    @Override
    public boolean targetsChosenByReflexiveAbility() {
        return reflexive;
    }
}
