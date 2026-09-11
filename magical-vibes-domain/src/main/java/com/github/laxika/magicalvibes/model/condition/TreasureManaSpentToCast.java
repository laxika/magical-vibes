package com.github.laxika.magicalvibes.model.condition;

/**
 * At least one mana produced by a Treasure was spent to cast this spell.
 * The result is snapshotted during payment and read while the spell resolves.
 */
public record TreasureManaSpentToCast() implements Condition {

    @Override
    public String conditionName() {
        return "Treasure mana spent to cast";
    }

    @Override
    public String conditionNotMetReason() {
        return "no Treasure mana was spent to cast this spell";
    }
}
