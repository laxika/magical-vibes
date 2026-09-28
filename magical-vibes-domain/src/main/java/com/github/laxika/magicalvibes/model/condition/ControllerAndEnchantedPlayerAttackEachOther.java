package com.github.laxika.magicalvibes.model.condition;

/**
 * True during an attack declaration when the source's controller attacks the enchanted player
 * or one of that player's planeswalkers, or when the enchanted player attacks the source's
 * controller or one of that player's planeswalkers.
 */
public record ControllerAndEnchantedPlayerAttackEachOther() implements Condition {

    @Override
    public String conditionName() {
        return "you and the enchanted player attack each other";
    }

    @Override
    public String conditionNotMetReason() {
        return "you and the enchanted player did not attack each other";
    }
}
