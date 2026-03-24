package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;

/**
 * Conditional wrapper that resolves its inner effect only when at least one
 * nontoken permanent with the specified subtype is attacking.
 * <p>
 * Used for triggered abilities that fire "whenever one or more nontoken [subtype]
 * you control attack" (e.g. Mavren Fein, Dusk Apostle).
 * <p>
 * The condition is checked at trigger time in {@code CombatAttackService}
 * (the trigger is not pushed if no nontoken creatures of the required subtype are
 * attacking) and re-checked at resolution time via
 * {@code EffectResolutionService.evaluateCondition()}.
 *
 * @param requiredSubtype the subtype that at least one nontoken attacker must have
 * @param wrapped         the inner effect to resolve when the condition is met
 */
public record HasNontokenSubtypeAttackerConditionalEffect(CardSubtype requiredSubtype, CardEffect wrapped) implements ConditionalEffect {

    @Override
    public String conditionName() {
        return "nontoken " + requiredSubtype.name().toLowerCase() + " attacker";
    }

    @Override
    public String conditionNotMetReason() {
        return "no nontoken " + requiredSubtype.name().toLowerCase() + " among attackers";
    }
}
