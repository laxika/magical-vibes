package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.Set;

/**
 * One-shot damage-prevention spell/ability: writes the shield state selected by {@link #scope()}.
 * Collapses the shield-creating {@code Prevent*Effect} family — every "prevent the next N damage
 * to <victim>" and "prevent all [combat] damage to/by <victim> this turn" wording is this record
 * with a scope, never a new effect class. Build instances via the static factories.
 *
 * <p>The consumption side (how shields absorb damage) lives unchanged in
 * {@code DamagePreventionService}; static always-on prevention markers with riders (Vigor, Purity,
 * Urza's Armor, CoP-style chosen-source shields, …) remain separate records.
 *
 * @param scope           which shield-state slot to write
 * @param amount          the shield size for {@code NEXT_*} scopes ({@code null} for ALL-style scopes)
 * @param combatOnly      combat-only window for the target-creature scopes (Foxfire)
 * @param sourceColors    the prevented source colors for {@link PreventionScope#ALL_FROM_COLORS}
 * @param exemptPredicate creatures still dealing combat damage for {@link PreventionScope#ALL_COMBAT_EXCEPT}
 */
public record PreventDamageEffect(
        PreventionScope scope,
        DynamicAmount amount,
        boolean combatOnly,
        Set<CardColor> sourceColors,
        PermanentPredicate exemptPredicate
) implements CardEffect {

    public PreventDamageEffect {
        boolean needsAmount = scope == PreventionScope.NEXT_TO_ANY
                || scope == PreventionScope.NEXT_TO_CONTROLLER
                || scope == PreventionScope.NEXT_TO_SELF
                || scope == PreventionScope.NEXT_TO_TARGET;
        if (needsAmount && amount == null) {
            throw new IllegalArgumentException("NEXT_* prevention scopes require an amount: " + scope);
        }
        if (!needsAmount && amount != null) {
            throw new IllegalArgumentException("ALL-style prevention scopes take no amount: " + scope);
        }
        if ((sourceColors != null) != (scope == PreventionScope.ALL_FROM_COLORS)) {
            throw new IllegalArgumentException("sourceColors is exactly the ALL_FROM_COLORS parameter: " + scope);
        }
        if ((exemptPredicate != null) != (scope == PreventionScope.ALL_COMBAT_EXCEPT)) {
            throw new IllegalArgumentException("exemptPredicate is exactly the ALL_COMBAT_EXCEPT parameter: " + scope);
        }
    }

    /** "Prevent the next {@code amount} damage that would be dealt to any permanent or player." */
    public static PreventDamageEffect nextToAny(int amount) {
        return new PreventDamageEffect(PreventionScope.NEXT_TO_ANY, new Fixed(amount), false, null, null);
    }

    /** "Prevent the next {@code amount} damage that would be dealt to you." */
    public static PreventDamageEffect nextToController(int amount) {
        return new PreventDamageEffect(PreventionScope.NEXT_TO_CONTROLLER, new Fixed(amount), false, null, null);
    }

    /** "Prevent the next {@code amount} damage that would be dealt to ~." */
    public static PreventDamageEffect nextToSelf(int amount) {
        return new PreventDamageEffect(PreventionScope.NEXT_TO_SELF, new Fixed(amount), false, null, null);
    }

    /** "Prevent the next {@code amount} damage that would be dealt to any target." */
    public static PreventDamageEffect nextToTarget(int amount) {
        return nextToTarget(new Fixed(amount));
    }

    /** "Prevent the next X damage that would be dealt to any target" (Alabaster Potion). */
    public static PreventDamageEffect nextToTarget(DynamicAmount amount) {
        return new PreventDamageEffect(PreventionScope.NEXT_TO_TARGET, amount, false, null, null);
    }

    /** "Prevent all combat damage that would be dealt this turn." */
    public static PreventDamageEffect allCombat() {
        return new PreventDamageEffect(PreventionScope.ALL_COMBAT, null, false, null, null);
    }

    /** "Prevent all damage that would be dealt to creatures this turn." */
    public static PreventDamageEffect allToCreatures() {
        return new PreventDamageEffect(PreventionScope.ALL_TO_CREATURES, null, false, null, null);
    }

    /** "Prevent all damage that would be dealt to target creature(s) this turn." */
    public static PreventDamageEffect allToTargetCreatures() {
        return new PreventDamageEffect(PreventionScope.ALL_TO_TARGET_CREATURES, null, false, null, null);
    }

    /** "Prevent all combat damage that would be dealt to target creature(s) this turn" (Foxfire). */
    public static PreventDamageEffect allCombatToTargetCreatures() {
        return new PreventDamageEffect(PreventionScope.ALL_TO_TARGET_CREATURES, null, true, null, null);
    }

    /** "Prevent all damage target creature(s) would deal this turn" (Soul Parry). */
    public static PreventDamageEffect allByTargetCreatures() {
        return new PreventDamageEffect(PreventionScope.ALL_BY_TARGET_CREATURES, null, false, null, null);
    }

    /** "Prevent all combat damage target creature(s) would deal this turn" (Foxfire, Inquisitor's Snare). */
    public static PreventDamageEffect allCombatByTargetCreatures() {
        return new PreventDamageEffect(PreventionScope.ALL_BY_TARGET_CREATURES, null, true, null, null);
    }

    /** "Until your next turn, prevent all damage target permanent would deal" (Gideon of the Trials +1). */
    public static PreventDamageEffect allByTargetPermanentUntilNextTurn() {
        return new PreventDamageEffect(PreventionScope.ALL_BY_TARGET_PERMANENT_UNTIL_NEXT_TURN, null, false, null, null);
    }

    /** "Prevent all damage that would be dealt to ~ this turn" — the source permanent (Gideon of the Trials 0). */
    public static PreventDamageEffect allToSelf() {
        return new PreventDamageEffect(PreventionScope.ALL_TO_SELF, null, false, null, null);
    }

    /** "Prevent all damage that would be dealt to you and creatures you control this turn." */
    public static PreventDamageEffect allToControllerAndCreatures() {
        return new PreventDamageEffect(PreventionScope.ALL_TO_CONTROLLER_AND_CREATURES, null, false, null, null);
    }

    /** "Prevent all damage attacking creatures would deal to you this turn" (Deep Wood). */
    public static PreventDamageEffect allToControllerFromAttackers() {
        return new PreventDamageEffect(PreventionScope.ALL_TO_CONTROLLER_FROM_ATTACKERS, null, false, null, null);
    }

    /** "Prevent all damage that sources of the given colors would deal this turn" (Luminesce). */
    public static PreventDamageEffect fromColors(Set<CardColor> colors) {
        return new PreventDamageEffect(PreventionScope.ALL_FROM_COLORS, null, false, colors, null);
    }

    /** "Prevent all combat damage this turn except that dealt by matching creatures" (Moonmist). */
    public static PreventDamageEffect allCombatExcept(PermanentPredicate exemptPredicate) {
        return new PreventDamageEffect(PreventionScope.ALL_COMBAT_EXCEPT, null, false, null, exemptPredicate);
    }

    @Override
    public TargetSpec targetSpec() {
        return switch (scope) {
            case NEXT_TO_TARGET -> TargetSpec.benign(TargetCategory.ANY_TARGET);
            case ALL_TO_TARGET_CREATURES, ALL_BY_TARGET_CREATURES -> TargetSpec.benign(TargetCategory.CREATURE);
            case ALL_BY_TARGET_PERMANENT_UNTIL_NEXT_TURN -> TargetSpec.benign(TargetCategory.PERMANENT);
            default -> TargetSpec.NONE;
        };
    }
}
