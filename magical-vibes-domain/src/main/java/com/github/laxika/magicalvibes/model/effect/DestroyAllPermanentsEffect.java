package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Destroys every permanent matching {@code filter} on the battlefield(s) selected by {@code scope},
 * then optionally resolves a {@code thenEffect} rider.
 *
 * <p>The number of permanents <em>actually destroyed</em> (indestructible and regenerated
 * permanents do not count) is snapshotted onto the rider's {@code StackEntry.eventValue} channel, so
 * "for each permanent destroyed this way" riders are built from existing effects on the
 * {@code EventValue} amount (Fracturing Gust = {@code GainLifeEffect(Scaled(EventValue(), 2))},
 * Phyrexian Rebirth = an X/X token with {@code EventValue} power/toughness). The rider always
 * resolves against the effect's controller, mirroring {@link DestroyTargetPermanentThenEffect}.
 *
 * @param filter              which permanents are destroyed
 * @param cannotBeRegenerated when {@code true} the destruction can't be prevented by regeneration
 * @param scope               every battlefield, or only the targeted player's (Rain of Daggers)
 * @param thenEffect          optional rider resolved after destruction with the destroyed count on
 *                            {@code eventValue}; {@code null} for a plain board wipe
 */
public record DestroyAllPermanentsEffect(
        PermanentPredicate filter,
        boolean cannotBeRegenerated,
        EachPermanentScope scope,
        CardEffect thenEffect
) implements BoardWipeEffect {

    public DestroyAllPermanentsEffect(PermanentPredicate filter) {
        this(filter, false, EachPermanentScope.ALL_PLAYERS, null);
    }

    public DestroyAllPermanentsEffect(PermanentPredicate filter, boolean cannotBeRegenerated) {
        this(filter, cannotBeRegenerated, EachPermanentScope.ALL_PLAYERS, null);
    }

    /** Board wipe with a per-destroyed-count rider ("You gain 2 life for each permanent destroyed this way"). */
    public DestroyAllPermanentsEffect(PermanentPredicate filter, CardEffect thenEffect) {
        this(filter, false, EachPermanentScope.ALL_PLAYERS, thenEffect);
    }

    /** Scoped destroy-all with a per-destroyed-count rider ("Destroy all creatures target player controls. …"). */
    public DestroyAllPermanentsEffect(PermanentPredicate filter, EachPermanentScope scope, CardEffect thenEffect) {
        this(filter, false, scope, thenEffect);
    }

    /** Destroy-all always sweeps the board. */
    @Override
    public boolean sweepsBoard() {
        return true;
    }
}
