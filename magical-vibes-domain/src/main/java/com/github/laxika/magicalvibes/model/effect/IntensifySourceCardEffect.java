package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

import java.util.Objects;

/**
 * Increases the persistent intensity of the physical card that supplies the effect by {@code amount}, evaluated as
 * the effect resolves ("intensifies by X, where X is that creature's power", Quickbeast Amulet); an amount of 0 or
 * less changes nothing.
 */
public record IntensifySourceCardEffect(DynamicAmount amount) implements CardEffect {

    public IntensifySourceCardEffect {
        Objects.requireNonNull(amount, "amount");
    }

    /** "Intensifies by N". */
    public IntensifySourceCardEffect(int amount) {
        this(new Fixed(positive(amount)));
    }

    private static int positive(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Intensity amount must be positive");
        }
        return amount;
    }
}
