package com.github.laxika.magicalvibes.model.effect;

/**
 * Conditional wrapper that only applies when the source permanent did not attack this turn.
 * Implements the intervening-if clause "if this creature didn't attack this turn".
 */
public record DidntAttackConditionalEffect(CardEffect wrapped) implements ConditionalEffect {

    @Override
    public String conditionName() {
        return "didn't attack";
    }

    @Override
    public String conditionNotMetReason() {
        return "this creature attacked this turn";
    }
}
