package com.github.laxika.magicalvibes.model.condition;

/**
 * The source permanent has dealt damage at least once since it became this game object.
 */
public record SourceHasDealtDamage() implements Condition {

    @Override
    public String conditionName() {
        return "has dealt damage";
    }

    @Override
    public String conditionNotMetReason() {
        return "it has not dealt damage yet";
    }
}
