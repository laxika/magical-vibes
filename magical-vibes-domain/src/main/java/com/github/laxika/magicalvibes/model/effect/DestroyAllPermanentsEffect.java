package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Destroys every permanent matching {@code filter} on the battlefield(s) selected by {@code scope}, as one event, and
 * records the step ({@code StepKind.DESTROY}): a later step of the same resolution reads what it did through
 * {@code AmountThisWay} ("you gain 1 life for each creature destroyed this way" is
 * {@code GainLifeEffect(new AmountThisWay(StepKind.DESTROY))}; "for each creature that died this way",
 * "for each nontoken creature destroyed this way" and "creatures you controlled that were destroyed this way" are its
 * {@code died}, {@code matching} and {@code controlledByYou} forms), {@code HappenedThisWay} and
 * {@code playersThisWay(StepKind.DESTROY)}. Indestructible and regenerated permanents are not destroyed; one a
 * replacement exiled instead of putting it into a graveyard was destroyed but didn't die.
 *
 * <p>{@code thenEffect} is the older form of a rider, kept for riders that act once per destroyed permanent for its
 * controller (From the Ashes, Seeds of Innocence, Stench of Evil): it resolves after the destruction with the
 * destroyed count on {@code StackEntry.eventValue} and the destroyed permanents' controllers and mana values on the
 * entry's event lists, against the effect's controller.
 *
 * @param filter                              which permanents are destroyed
 * @param cannotBeRegenerated                 when {@code true} the destruction can't be prevented by regeneration
 * @param scope                               every battlefield, or only the targeted player's (Rain of Daggers)
 * @param thenEffect                          optional per-destroyed-permanent rider resolved after destruction;
 *                                            {@code null} for a plain board wipe
 * @param spared                              permanents named by an earlier step of the same resolution that the
 *                                            wipe leaves ("destroy all <em>other</em> creatures" after creating
 *                                            tokens, Martial Coup; "destroy the rest" after a choice, Duneblast), or
 *                                            {@code null}
 * @param referencedTarget                    the target a filter amount reads but the wipe doesn't destroy for being
 *                                            it ("power greater than target creature's power", Fell the Mighty), or
 *                                            {@code null}; the spell declares it, and is countered if it is illegal
 *                                            (CR 608.2b)
 */
public record DestroyAllPermanentsEffect(
        PermanentPredicate filter,
        boolean cannotBeRegenerated,
        EachPermanentScope scope,
        CardEffect thenEffect,
        PermanentSubject spared,
        TargetSpec referencedTarget
) implements BoardWipeEffect {

    public DestroyAllPermanentsEffect(PermanentPredicate filter, boolean cannotBeRegenerated, EachPermanentScope scope,
                                      CardEffect thenEffect, PermanentSubject spared) {
        this(filter, cannotBeRegenerated, scope, thenEffect, spared, null);
    }

    public DestroyAllPermanentsEffect(PermanentPredicate filter) {
        this(filter, false, EachPermanentScope.ALL_PLAYERS, null, null);
    }

    public DestroyAllPermanentsEffect(PermanentPredicate filter, boolean cannotBeRegenerated) {
        this(filter, cannotBeRegenerated, EachPermanentScope.ALL_PLAYERS, null, null);
    }

    /** "Destroy all creatures target player controls": the targeted player's battlefield only. */
    public DestroyAllPermanentsEffect(PermanentPredicate filter, EachPermanentScope scope) {
        this(filter, false, scope, null, null);
    }

    /** Board wipe with a per-destroyed-permanent rider ("For each land destroyed this way, its controller …"). */
    public DestroyAllPermanentsEffect(PermanentPredicate filter, CardEffect thenEffect) {
        this(filter, false, EachPermanentScope.ALL_PLAYERS, thenEffect, null);
    }

    /**
     * "Destroy all other creatures" style wipe that spares permanents created earlier in this same
     * resolution — e.g. Martial Coup, which makes its Soldier tokens first and then destroys every
     * <em>other</em> creature. Pair with an unconditional {@code CreateTokenEffect} resolving before it.
     */
    public static DestroyAllPermanentsEffect sparingPermanentsCreatedThisResolution(PermanentPredicate filter) {
        return sparing(filter, Subjects.createdThisResolution());
    }

    /** "Destroy the rest", "destroy all other [filter]": all matching permanents but those {@code spared} names. */
    public static DestroyAllPermanentsEffect sparing(PermanentPredicate filter, PermanentSubject spared) {
        return new DestroyAllPermanentsEffect(filter, false, EachPermanentScope.ALL_PLAYERS, null, spared);
    }

    /**
     * "Destroy all creatures with power greater than target creature's power": the wipe over {@code filter}, whose
     * amounts ({@code PermanentPowerAtLeastAmountPredicate} / {@code PermanentPowerAtMostAmountPredicate} over
     * {@code TargetPower}) read the target declared by {@code target}.
     */
    public static DestroyAllPermanentsEffect readingTarget(PermanentPredicate filter, TargetPredicate target) {
        return new DestroyAllPermanentsEffect(filter, false, EachPermanentScope.ALL_PLAYERS, null, null,
                TargetSpec.harmful(target));
    }

    /** Destroy-all always sweeps the board. */
    @Override
    public boolean sweepsBoard() {
        return true;
    }

    /**
     * The {@link EachPermanentScope#TARGET_PLAYER} scope needs the player whose battlefield is
     * swept — "destroy all creatures target opponent controls", "destroy all lands target player
     * controls". Same shape as the sibling scoped effect
     * ({@link PutCounterOnEachMatchingPermanentEffect}); the every-battlefield scope targets nothing.
     */
    @Override
    public TargetSpec targetSpec() {
        if (scope == EachPermanentScope.TARGET_PLAYER) {
            return TargetSpec.harmful(TargetPredicates.player());
        }
        return referencedTarget != null ? referencedTarget : TargetSpec.NONE;
    }
}
