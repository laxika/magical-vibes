package com.github.laxika.magicalvibes.model.condition;

/** At least one mana produced by a Treasure was spent to activate the source ability. */
public record TreasureManaSpentToActivate() implements Condition {

    @Override
    public String conditionName() {
        return "Treasure mana spent to activate this ability";
    }

    @Override
    public String conditionNotMetReason() {
        return "no Treasure mana was spent to activate this ability";
    }
}
