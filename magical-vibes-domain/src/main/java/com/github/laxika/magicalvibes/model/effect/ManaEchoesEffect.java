package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;

import java.util.Set;

/** Resolves Mana Echoes' optional colorless-mana trigger. */
public record ManaEchoesEffect(Set<CardSubtype> enteringCreatureTypes) implements CardEffect {

    public ManaEchoesEffect {
        enteringCreatureTypes = enteringCreatureTypes == null ? Set.of() : Set.copyOf(enteringCreatureTypes);
    }

    public ManaEchoesEffect() {
        this(Set.of());
    }
}
