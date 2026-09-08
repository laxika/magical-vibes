package com.github.laxika.magicalvibes.model.condition;

/**
 * At least one mana produced by a snow source was spent to cast this spell.
 * The amount is snapshotted at cast time and read back while the spell or its
 * enter-the-battlefield ability resolves.
 */
public record SnowManaSpentToCast() implements Condition {

    @Override
    public String conditionName() {
        return "snow mana spent to cast";
    }

    @Override
    public String conditionNotMetReason() {
        return "no snow mana was spent to cast this spell";
    }
}
