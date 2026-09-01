package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;

import java.util.Set;

/**
 * Static marker for an effect that makes spells controlled by the source permanent's controller
 * unable to be countered. The optional minimum mana value restricts the protection to spells whose
 * mana value meets that threshold. The optional card-type set restricts the protection to spells
 * with at least one of those types.
 */
public record ControllerSpellsCantBeCounteredEffect(boolean noncreatureOnly,
                                                    Integer minimumManaValue,
                                                    Set<CardType> cardTypes) implements CardEffect {

    public ControllerSpellsCantBeCounteredEffect {
        cardTypes = cardTypes == null ? Set.of() : Set.copyOf(cardTypes);
    }

    public ControllerSpellsCantBeCounteredEffect() {
        this(false, null, Set.of());
    }

    public ControllerSpellsCantBeCounteredEffect(boolean noncreatureOnly) {
        this(noncreatureOnly, null, Set.of());
    }

    public ControllerSpellsCantBeCounteredEffect(boolean noncreatureOnly, Integer minimumManaValue) {
        this(noncreatureOnly, minimumManaValue, Set.of());
    }

    public ControllerSpellsCantBeCounteredEffect(int minimumManaValue) {
        this(false, minimumManaValue, Set.of());
    }

    public ControllerSpellsCantBeCounteredEffect(Set<CardType> cardTypes) {
        this(false, null, cardTypes);
    }
}
