package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;

import java.util.Objects;

/**
 * At resolution, choose a matching permanent you control or reveal a matching card from your
 * hand. The chosen object stays where it is; {@code thenEffect} resolves only after a valid
 * object has been beheld.
 */
public record BeholdEffect(CardSubtype subtype, CardEffect thenEffect) implements CardEffect {

    public BeholdEffect {
        Objects.requireNonNull(subtype, "subtype");
        Objects.requireNonNull(thenEffect, "thenEffect");
    }
}
