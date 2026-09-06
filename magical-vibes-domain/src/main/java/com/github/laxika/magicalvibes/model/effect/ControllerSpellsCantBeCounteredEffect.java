package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

import java.util.Set;

/**
 * Static marker for an effect that makes spells controlled by the source permanent's controller
 * unable to be countered. The optional minimum mana value restricts the protection to spells whose
 * mana value meets that threshold. The optional card-type set and card predicate further restrict
 * the protected spells.
 */
public record ControllerSpellsCantBeCounteredEffect(boolean noncreatureOnly,
                                                    Integer minimumManaValue,
                                                    Set<CardType> cardTypes,
                                                    CardPredicate predicate) implements CardEffect {

    public ControllerSpellsCantBeCounteredEffect {
        cardTypes = cardTypes == null ? Set.of() : Set.copyOf(cardTypes);
    }

    public ControllerSpellsCantBeCounteredEffect() {
        this(false, null, Set.of(), null);
    }

    public ControllerSpellsCantBeCounteredEffect(boolean noncreatureOnly) {
        this(noncreatureOnly, null, Set.of(), null);
    }

    public ControllerSpellsCantBeCounteredEffect(boolean noncreatureOnly, Integer minimumManaValue) {
        this(noncreatureOnly, minimumManaValue, Set.of(), null);
    }

    public ControllerSpellsCantBeCounteredEffect(int minimumManaValue) {
        this(false, minimumManaValue, Set.of(), null);
    }

    public ControllerSpellsCantBeCounteredEffect(Set<CardType> cardTypes) {
        this(false, null, cardTypes, null);
    }

    public ControllerSpellsCantBeCounteredEffect(CardPredicate predicate) {
        this(false, null, Set.of(), predicate);
    }
}
