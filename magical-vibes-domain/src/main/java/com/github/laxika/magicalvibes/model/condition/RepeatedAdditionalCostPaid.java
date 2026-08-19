package com.github.laxika.magicalvibes.model.condition;

/**
 * A particular payment declared by a repeatable additional mana cost was paid to cast the source
 * spell.
 */
public record RepeatedAdditionalCostPaid(String manaCost) implements Condition {

    public RepeatedAdditionalCostPaid {
        if (manaCost == null || manaCost.isBlank()) {
            throw new IllegalArgumentException("manaCost must not be blank");
        }
    }

    @Override
    public String conditionName() {
        return manaCost + " additional cost paid";
    }

    @Override
    public String conditionNotMetReason() {
        return manaCost + " additional cost was not paid";
    }
}
