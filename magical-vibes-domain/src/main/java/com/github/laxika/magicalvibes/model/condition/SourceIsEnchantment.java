package com.github.laxika.magicalvibes.model.condition;

/** The source permanent is an enchantment. */
public record SourceIsEnchantment() implements Condition {

    @Override
    public String conditionName() {
        return "source is an enchantment";
    }

    @Override
    public String conditionNotMetReason() {
        return "source is not an enchantment";
    }
}
