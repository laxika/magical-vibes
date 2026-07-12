package com.github.laxika.magicalvibes.model.condition;

import com.github.laxika.magicalvibes.model.ManaColor;

/**
 * At least one mana of {@code color} was spent to cast this spell (including generic costs
 * paid with that color). The set of colors spent is snapshotted at cast time into
 * {@code GameData.spellCastColorsSpent} and read back when the spell resolves. Used by
 * spells whose clauses are gated on which hybrid color was paid (e.g. Repel Intruders).
 */
public record ColorSpentToCast(ManaColor color) implements Condition {

    @Override
    public String conditionName() {
        return color.getCode() + " spent to cast";
    }

    @Override
    public String conditionNotMetReason() {
        return "no " + color.name().toLowerCase() + " mana was spent to cast this spell";
    }
}
