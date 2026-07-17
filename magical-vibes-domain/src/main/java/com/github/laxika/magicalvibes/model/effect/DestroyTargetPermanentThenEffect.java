package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * "Destroy target permanent, then [thenEffect]." Collapses the destroy-plus-value family: the
 * targeted permanent is destroyed and an existing {@code thenEffect} is resolved afterwards, reusing
 * that effect's own handler rather than reimplementing life gain / life loss / poison / self-boost.
 *
 * <p>The then-effect is routed to the right player by {@link #recipient()}: {@code CONTROLLER}
 * resolves it against the spell/ability controller (you gain life, this permanent gets +X/+0), while
 * {@code TARGET_CONTROLLER} resolves it against the controller of the destroyed permanent
 * (snapshotted before destruction — "its controller loses N life"). Because the destroyed
 * permanent's mana value / toughness cannot be recomputed once it leaves the battlefield,
 * {@link #stat()} snapshots that last-known value onto the stack entry's {@code eventValue} channel
 * before destruction so a then-effect built on the {@code EventValue} amount reads it correctly.
 *
 * <p>The then-effect always happens regardless of whether destruction actually succeeds
 * (indestructible / regeneration), matching the "second sentence" wording of every card that maps
 * here — none of them gate the then-effect on the permanent being put into a graveyard. When
 * {@link #thenCondition()} is non-null the then-effect happens only if the destroyed permanent
 * matched that predicate (Death's Caress: "If that creature was a Human, you gain life equal to its
 * toughness."), evaluated on last-known information before the permanent leaves.
 *
 * @param stat                 last-known stat to snapshot onto {@code eventValue} before destruction
 * @param thenEffect           an existing effect resolved after destruction (reused via its own handler)
 * @param recipient            whose controller slot the then-effect acts on
 * @param thenCondition        when non-null, the then-effect happens only if the destroyed permanent matched it
 * @param cannotBeRegenerated  when {@code true} the destruction can't be prevented by regeneration (Crumble)
 */
public record DestroyTargetPermanentThenEffect(
        EventStat stat,
        CardEffect thenEffect,
        ThenEffectRecipient recipient,
        PermanentPredicate thenCondition,
        boolean cannotBeRegenerated
) implements CardEffect {

    /** Unconditional then-effect, no last-known stat snapshot. */
    public DestroyTargetPermanentThenEffect(CardEffect thenEffect, ThenEffectRecipient recipient) {
        this(EventStat.NONE, thenEffect, recipient, null, false);
    }

    /** Then-effect reading a snapshotted stat, no extra condition. */
    public DestroyTargetPermanentThenEffect(EventStat stat, CardEffect thenEffect, ThenEffectRecipient recipient) {
        this(stat, thenEffect, recipient, null, false);
    }

    /** Then-effect reading a snapshotted stat, destruction can't be regenerated (Crumble). */
    public DestroyTargetPermanentThenEffect(EventStat stat, CardEffect thenEffect, ThenEffectRecipient recipient,
                                            boolean cannotBeRegenerated) {
        this(stat, thenEffect, recipient, null, cannotBeRegenerated);
    }

    /** Then-effect reading a snapshotted stat, gated on a resolution-time condition. */
    public DestroyTargetPermanentThenEffect(EventStat stat, CardEffect thenEffect, ThenEffectRecipient recipient,
                                            PermanentPredicate thenCondition) {
        this(stat, thenEffect, recipient, thenCondition, false);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetCategory.PERMANENT);
    }
}
