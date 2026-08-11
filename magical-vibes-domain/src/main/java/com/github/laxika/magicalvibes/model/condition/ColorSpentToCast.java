package com.github.laxika.magicalvibes.model.condition;

import com.github.laxika.magicalvibes.model.ManaColor;

/**
 * At least {@code minimumAmount} mana of {@code color} was spent to cast this spell (including
 * generic costs paid with that color). The per-color amounts are snapshotted at cast time into
 * {@code GameData.spellCastManaSpentByColor} and read back when the spell resolves.
 */
public record ColorSpentToCast(ManaColor color, int minimumAmount) implements Condition {

    public ColorSpentToCast(ManaColor color) {
        this(color, 1);
    }

    public ColorSpentToCast {
        if (minimumAmount < 1) {
            throw new IllegalArgumentException("minimumAmount must be positive");
        }
    }

    @Override
    public String conditionName() {
        return minimumAmount == 1
                ? color.getCode() + " spent to cast"
                : "at least " + minimumAmount + " " + color.getCode() + " mana spent to cast";
    }

    @Override
    public String conditionNotMetReason() {
        return minimumAmount == 1
                ? "no " + color.name().toLowerCase() + " mana was spent to cast this spell"
                : "fewer than " + minimumAmount + " " + color.name().toLowerCase()
                        + " mana was spent to cast this spell";
    }
}
