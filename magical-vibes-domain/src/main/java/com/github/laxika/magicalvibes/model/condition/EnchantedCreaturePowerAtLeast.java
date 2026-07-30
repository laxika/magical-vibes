package com.github.laxika.magicalvibes.model.condition;

/** The creature the source Aura is attached to has effective power {@code threshold} or greater (Arachnus Web). */
public record EnchantedCreaturePowerAtLeast(int threshold) implements Condition {

    @Override
    public String conditionName() {
        return "enchanted creature's power is " + threshold + " or greater";
    }

    @Override
    public String conditionNotMetReason() {
        return "the enchanted creature's power is less than " + threshold;
    }
}
