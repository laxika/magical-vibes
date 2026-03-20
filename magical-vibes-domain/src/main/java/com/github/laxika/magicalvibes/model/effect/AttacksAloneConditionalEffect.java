package com.github.laxika.magicalvibes.model.effect;

/**
 * Conditional wrapper that resolves its inner effect only when the source creature
 * attacks alone (is the only creature declared as an attacker).
 * <p>
 * Per CR 506.5, a creature "attacks alone" if it's the only creature declared as
 * an attacker during the declare attackers step. The condition is checked at trigger
 * time (the trigger only goes on the stack if the creature attacks alone) and
 * re-checked at resolution time (intervening-if pattern).
 *
 * @param wrapped the inner effect to resolve when the creature attacks alone
 */
public record AttacksAloneConditionalEffect(CardEffect wrapped) implements ConditionalEffect {

    @Override
    public String conditionName() {
        return "attacks alone";
    }

    @Override
    public String conditionNotMetReason() {
        return "creature did not attack alone";
    }
}
