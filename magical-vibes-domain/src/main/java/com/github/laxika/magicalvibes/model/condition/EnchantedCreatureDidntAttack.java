package com.github.laxika.magicalvibes.model.condition;

/** The creature the source Aura is attached to did not attack this turn (Aggression). */
public record EnchantedCreatureDidntAttack() implements Condition {

    @Override
    public String conditionName() {
        return "enchanted creature didn't attack";
    }

    @Override
    public String conditionNotMetReason() {
        return "the enchanted creature attacked this turn";
    }
}
