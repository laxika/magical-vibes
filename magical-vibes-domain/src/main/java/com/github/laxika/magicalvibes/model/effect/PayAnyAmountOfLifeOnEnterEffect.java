package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

/**
 * "As this creature enters, pay any amount of life." A replacement effect made as the permanent
 * enters, before enter-the-battlefield triggers: the controller picks a number between 0 and their
 * current life total, optionally limited by {@code maxAmount}, loses that much life, and the amount
 * is stored on the permanent via {@code Permanent.setChosenNumber(int)} so a characteristic-defining
 * power/toughness can read it back with
 * {@link com.github.laxika.magicalvibes.model.amount.ChosenNumberOnSource}.
 */
public record PayAnyAmountOfLifeOnEnterEffect(DynamicAmount maxAmount) implements ReplacementEffect {

    /** The unrestricted form used by Minion of the Wastes. */
    public PayAnyAmountOfLifeOnEnterEffect() {
        this(null);
    }
}
