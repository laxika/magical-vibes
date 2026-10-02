package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;

import java.util.List;
import java.util.Set;

/** Records perpetual type, subtype, and activated-ability grants on the card chosen by a search. */
public record PerpetuallyGrantChosenCardCharacteristicsEffect(
        Set<CardType> cardTypes,
        Set<CardSubtype> subtypes,
        List<ActivatedAbility> activatedAbilities
) implements CardEffect {

    public PerpetuallyGrantChosenCardCharacteristicsEffect {
        cardTypes = Set.copyOf(cardTypes);
        subtypes = Set.copyOf(subtypes);
        activatedAbilities = List.copyOf(activatedAbilities);
    }
}
