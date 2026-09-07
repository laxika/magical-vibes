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
 * {@link EventStat#BASIC_LAND_SEARCH_COUNT} is the exception: it snapshots one or two based on
 * whether the targeted permanent was a land that was actually destroyed.
 *
 * <p>By default the then-effect happens regardless of whether destruction actually succeeds
 * (indestructible / regeneration), matching the "second sentence" wording of most cards that map
 * here. Cards worded "if … dies this way" instead require the permanent to actually reach the
 * graveyard; set {@link #requiresDestruction()} for those (Cinder Cloud). When
 * {@link #thenCondition()} is non-null the then-effect happens only if the destroyed permanent
 * matched that predicate (Death's Caress: "If that creature was a Human, you gain life equal to its
 * toughness."), evaluated on last-known information before the permanent leaves.
 *
 * @param stat                 last-known stat to snapshot onto {@code eventValue} before destruction
 * @param thenEffect           an existing effect resolved after destruction (reused via its own handler)
 * @param recipient            whose controller slot the then-effect acts on
 * @param thenCondition        when non-null, the then-effect happens only if the destroyed permanent matched it
 * @param cannotBeRegenerated  when {@code true} the destruction can't be prevented by regeneration (Crumble)
 * @param requiresDestruction  when {@code true} the then-effect happens only if the permanent was actually destroyed
 */
public record DestroyTargetPermanentThenEffect(
        EventStat stat,
        CardEffect thenEffect,
        ThenEffectRecipient recipient,
        PermanentPredicate thenCondition,
        boolean cannotBeRegenerated,
        boolean requiresDestruction
) implements CardEffect {

    /** Unconditional then-effect, no last-known stat snapshot. */
    public DestroyTargetPermanentThenEffect(CardEffect thenEffect, ThenEffectRecipient recipient) {
        this(EventStat.NONE, thenEffect, recipient, null, false, false);
    }

    /** Then-effect reading a snapshotted stat, no extra condition. */
    public DestroyTargetPermanentThenEffect(EventStat stat, CardEffect thenEffect, ThenEffectRecipient recipient) {
        this(stat, thenEffect, recipient, null, false, false);
    }

    /** Then-effect reading a snapshotted stat, destruction can't be regenerated (Crumble). */
    public DestroyTargetPermanentThenEffect(EventStat stat, CardEffect thenEffect, ThenEffectRecipient recipient,
                                            boolean cannotBeRegenerated) {
        this(stat, thenEffect, recipient, null, cannotBeRegenerated, false);
    }

    /** Then-effect reading a snapshotted stat, gated on a resolution-time condition. */
    public DestroyTargetPermanentThenEffect(EventStat stat, CardEffect thenEffect, ThenEffectRecipient recipient,
                                            PermanentPredicate thenCondition) {
        this(stat, thenEffect, recipient, thenCondition, false, false);
    }

    /** "If a … creature dies this way": condition plus a requirement that the permanent actually died. */
    public DestroyTargetPermanentThenEffect(EventStat stat, CardEffect thenEffect, ThenEffectRecipient recipient,
                                            PermanentPredicate thenCondition, boolean requiresDestruction) {
        this(stat, thenEffect, recipient, thenCondition, false, requiresDestruction);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.permanent());
    }
}
