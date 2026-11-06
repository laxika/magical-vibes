package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;

import java.util.List;
import java.util.Set;

/**
 * Records card types, subtypes, and activated abilities that follow the source's card identity between zones. A grant
 * to the card a choice selected is {@link PerpetuallyGrantChosenCardCharacteristicsEffect}.
 *
 * <p>When {@code replacesCreatureTypes} is set the granted subtypes are the card's new creature types,
 * replacing the ones it had ("perpetually becomes a Dragon"); otherwise they are added to the existing
 * subtypes ("in addition to its other types").
 */
public record PerpetuallyGrantCardCharacteristicsEffect(
        Set<CardType> cardTypes,
        Set<CardSubtype> subtypes,
        List<ActivatedAbility> activatedAbilities,
        boolean replacesCreatureTypes
) implements CardEffect {

    public PerpetuallyGrantCardCharacteristicsEffect {
        cardTypes = Set.copyOf(cardTypes);
        subtypes = Set.copyOf(subtypes);
        activatedAbilities = List.copyOf(activatedAbilities);
    }

    public PerpetuallyGrantCardCharacteristicsEffect(Set<CardType> cardTypes, Set<CardSubtype> subtypes,
                                                       List<ActivatedAbility> activatedAbilities) {
        this(cardTypes, subtypes, activatedAbilities, false);
    }

    /** The card perpetually becomes the given creature types, replacing the creature types it had. */
    public static PerpetuallyGrantCardCharacteristicsEffect becomingCreatureTypes(Set<CardSubtype> creatureTypes) {
        return new PerpetuallyGrantCardCharacteristicsEffect(Set.of(), creatureTypes, List.of(), true);
    }
}
