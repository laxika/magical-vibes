package com.github.laxika.magicalvibes.model.condition;

/** The source permanent has at least {@code minimum} Auras attached to it. */
public record EnchantedByAtLeastAuras(int minimum) implements Condition {

    @Override
    public String conditionName() {
        return "enchanted by at least " + minimum + " Auras";
    }

    @Override
    public String conditionNotMetReason() {
        return "enchanted by fewer than " + minimum + " Auras";
    }
}
