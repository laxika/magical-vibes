package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Deals damage to target creature or planeswalker (not player). The amount is a
 * {@link DynamicAmount} evaluated at resolution (fixed number, controlled permanent count, …).
 *
 * <p>An optional {@code targetRestriction} narrows the legal target (e.g. red only for
 * Chandra's Defeat); {@code null} means any creature or planeswalker is legal.
 *
 * <p>{@code exileInsteadOfDie} exiles the target if it would die this turn ("if that creature would die
 * this turn"), whether or not damage was dealt. With {@code exileOnlyIfDealtDamage} only a permanent this
 * effect actually dealt damage to is exiled ("if a permanent dealt damage this way would die this turn").
 */
public record DealDamageToTargetCreatureOrPlaneswalkerEffect(
        DynamicAmount damage, PermanentPredicate targetRestriction,
        boolean exileInsteadOfDie, boolean exileOnlyIfDealtDamage)
        implements DamageDealingEffect, CombatDamageAmountAwareEffect {

    public DealDamageToTargetCreatureOrPlaneswalkerEffect(DynamicAmount damage,
                                                           PermanentPredicate targetRestriction,
                                                           boolean exileInsteadOfDie) {
        this(damage, targetRestriction, exileInsteadOfDie, false);
    }

    /** "If a permanent dealt damage this way would die this turn, exile it instead." */
    public static DealDamageToTargetCreatureOrPlaneswalkerEffect exilingIfDealtDamage(DynamicAmount damage) {
        return new DealDamageToTargetCreatureOrPlaneswalkerEffect(damage, null, true, true);
    }

    public DealDamageToTargetCreatureOrPlaneswalkerEffect(DynamicAmount damage,
                                                           PermanentPredicate targetRestriction) {
        this(damage, targetRestriction, false);
    }

    public DealDamageToTargetCreatureOrPlaneswalkerEffect(int damage) {
        this(new Fixed(damage), null, false);
    }

    public DealDamageToTargetCreatureOrPlaneswalkerEffect(int damage, PermanentPredicate targetRestriction) {
        this(new Fixed(damage), targetRestriction, false);
    }

    public DealDamageToTargetCreatureOrPlaneswalkerEffect(int damage,
                                                           PermanentPredicate targetRestriction,
                                                           boolean exileInsteadOfDie) {
        this(new Fixed(damage), targetRestriction, exileInsteadOfDie);
    }

    public DealDamageToTargetCreatureOrPlaneswalkerEffect(DynamicAmount damage) {
        this(damage, null, false);
    }

    public DealDamageToTargetCreatureOrPlaneswalkerEffect(DynamicAmount damage,
                                                           boolean exileInsteadOfDie) {
        this(damage, null, exileInsteadOfDie);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.creatureOrPlaneswalker(), targetRestriction);
    }

    @Override
    public DynamicAmount damageAmount() {
        return damage;
    }

    @Override
    public DynamicAmount combatDamageAmount() {
        return damage;
    }

    @Override
    public CardEffect snapshotCombatDamage(int damageDealt) {
        return new DealDamageToTargetCreatureOrPlaneswalkerEffect(
                new Fixed(damageDealt), targetRestriction, exileInsteadOfDie, exileOnlyIfDealtDamage);
    }

    @Override
    public boolean canDamageCreatures() {
        return true;
    }

    @Override
    public boolean canDamagePlayers() {
        return false;
    }
}
