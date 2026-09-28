package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;

import java.util.List;
import java.util.Set;

/** Records card types, subtypes, and activated abilities that follow a card identity between zones. */
public record PerpetuallyGrantCardCharacteristicsEffect(
        Set<CardType> cardTypes,
        Set<CardSubtype> subtypes,
        List<ActivatedAbility> activatedAbilities
) implements CardEffect {

    public PerpetuallyGrantCardCharacteristicsEffect {
        cardTypes = Set.copyOf(cardTypes);
        subtypes = Set.copyOf(subtypes);
        activatedAbilities = List.copyOf(activatedAbilities);
    }

    public PerpetuallyGrantCardCharacteristicsEffect(CardType cardType, CardSubtype subtype,
                                                       ActivatedAbility activatedAbility) {
        this(Set.of(cardType), Set.of(subtype), List.of(activatedAbility));
    }
}
