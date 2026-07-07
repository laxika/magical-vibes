package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * "Destroy target permanent, then [rider]." Collapses the destroy-plus-value family: the targeted
 * permanent is destroyed and an existing {@code rider} effect is resolved afterwards, reusing that
 * effect's own handler rather than reimplementing life gain / life loss / poison / self-boost.
 *
 * <p>The rider is routed to the right player by {@link #recipient()}: {@code CONTROLLER} resolves it
 * against the spell/ability controller (you gain life, this permanent gets +X/+0), while
 * {@code TARGET_CONTROLLER} resolves it against the controller of the destroyed permanent
 * (snapshotted before destruction — "its controller loses N life"). Because the destroyed
 * permanent's mana value / toughness cannot be recomputed once it leaves the battlefield,
 * {@link #stat()} snapshots that last-known value onto the stack entry's {@code eventValue} channel
 * before destruction so a rider built on the {@code EventValue} amount reads it correctly.
 *
 * <p>The rider always happens regardless of whether destruction actually succeeds
 * (indestructible / regeneration), matching the "second sentence" wording of every card that maps
 * here — none of them gate the rider on the permanent being put into a graveyard. When
 * {@link #riderCondition()} is non-null the rider happens only if the destroyed permanent matched
 * that predicate (Death's Caress: "If that creature was a Human, you gain life equal to its
 * toughness."), evaluated on last-known information before the permanent leaves.
 *
 * @param stat          last-known stat to snapshot onto {@code eventValue} before destruction
 * @param rider         an existing effect resolved after destruction (reused via its own handler)
 * @param recipient     whose controller slot the rider acts on
 * @param riderCondition when non-null, the rider happens only if the destroyed permanent matched it
 */
public record DestroyTargetPermanentThenEffect(
        EventStat stat,
        CardEffect rider,
        RiderRecipient recipient,
        PermanentPredicate riderCondition
) implements CardEffect {

    /** Unconditional rider, no last-known stat snapshot. */
    public DestroyTargetPermanentThenEffect(CardEffect rider, RiderRecipient recipient) {
        this(EventStat.NONE, rider, recipient, null);
    }

    /** Rider reading a snapshotted stat, no extra condition. */
    public DestroyTargetPermanentThenEffect(EventStat stat, CardEffect rider, RiderRecipient recipient) {
        this(stat, rider, recipient, null);
    }

    @Override
    public boolean canTargetPermanent() {
        return true;
    }

    @Override
    public boolean isDamageOrDestruction() {
        return true;
    }
}
