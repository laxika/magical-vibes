package com.github.laxika.magicalvibes.model.condition;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * The permanent the source Aura is attached to matches {@code filter} (Nature's Chosen's
 * "Activate only if enchanted creature is white"). False when the source is not an attached Aura.
 *
 * @param filter      predicate the enchanted permanent must match
 * @param description human-readable description of the requirement, e.g. "enchanted creature is white"
 */
public record EnchantedPermanentMatches(PermanentPredicate filter, String description) implements Condition {

    @Override
    public String conditionName() {
        return description;
    }

    @Override
    public String conditionNotMetReason() {
        return "the condition \"" + description + "\" is not met";
    }

    @Override
    public boolean isEtbTriggerGate() {
        return true;
    }
}
