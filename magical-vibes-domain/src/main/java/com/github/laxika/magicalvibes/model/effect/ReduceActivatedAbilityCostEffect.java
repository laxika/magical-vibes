package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Reduces the generic mana portion of activated abilities of permanents matching {@code predicate}
 * by {@code amount}, for all players (static, symmetric). E.g. Heartstone with a creature
 * predicate and amount 1. The reduction can optionally preserve a minimum total cost of one
 * mana. {@code abilityKind} narrows it to one kind of ability; any kind but {@code ANY} is read only for the
 * abilities its controller activates ("Boast abilities you activate cost {1} less for each Dragon you control":
 * {@code (new PermanentTruePredicate(), dragons, ActivatedAbilityKind.BOAST)}).
 */
public record ReduceActivatedAbilityCostEffect(PermanentPredicate predicate, DynamicAmount amount,
                                               boolean appliesSymmetrically,
                                               boolean preventsReductionBelowOneMana,
                                               ActivatedAbilityKind abilityKind)
        implements ActivatedAbilityCostReducingEffect {

    public ReduceActivatedAbilityCostEffect(PermanentPredicate predicate, DynamicAmount amount,
                                            boolean appliesSymmetrically,
                                            boolean preventsReductionBelowOneMana, boolean powerUpOnly) {
        this(predicate, amount, appliesSymmetrically, preventsReductionBelowOneMana,
                powerUpOnly ? ActivatedAbilityKind.POWER_UP : ActivatedAbilityKind.ANY);
    }

    /** "[Kind] abilities you activate of [predicate] permanents cost {amount} less". */
    public ReduceActivatedAbilityCostEffect(PermanentPredicate predicate, DynamicAmount amount,
                                            ActivatedAbilityKind abilityKind) {
        this(predicate, amount, false, false, abilityKind);
    }

    public ReduceActivatedAbilityCostEffect(PermanentPredicate predicate, DynamicAmount amount) {
        this(predicate, amount, true, false, ActivatedAbilityKind.ANY);
    }

    public ReduceActivatedAbilityCostEffect(PermanentPredicate predicate, DynamicAmount amount,
                                            boolean appliesSymmetrically) {
        this(predicate, amount, appliesSymmetrically, false, ActivatedAbilityKind.ANY);
    }

    public ReduceActivatedAbilityCostEffect(PermanentPredicate predicate, int amount) {
        this(predicate, new Fixed(amount));
    }

    public ReduceActivatedAbilityCostEffect(PermanentPredicate predicate, int amount,
                                            boolean appliesSymmetrically,
                                            boolean preventsReductionBelowOneMana) {
        this(predicate, new Fixed(amount), appliesSymmetrically, preventsReductionBelowOneMana,
                ActivatedAbilityKind.ANY);
    }

    public ReduceActivatedAbilityCostEffect(PermanentPredicate predicate, int amount, boolean powerUpOnly) {
        this(predicate, new Fixed(amount), !powerUpOnly, false,
                powerUpOnly ? ActivatedAbilityKind.POWER_UP : ActivatedAbilityKind.ANY);
    }

    @Override
    public PermanentPredicate affectedPermanents() {
        return predicate;
    }

    @Override
    public int genericCostReduction() {
        return amount instanceof Fixed fixed ? fixed.value() : 0;
    }

    @Override
    public DynamicAmount genericCostReductionAmount() {
        return amount instanceof Fixed ? null : amount;
    }

    @Override
    public boolean appliesTo(ActivatedAbility ability) {
        return abilityKind.matches(ability);
    }

    @Override
    public boolean appliesSymmetrically() {
        return appliesSymmetrically && abilityKind == ActivatedAbilityKind.ANY;
    }
}
