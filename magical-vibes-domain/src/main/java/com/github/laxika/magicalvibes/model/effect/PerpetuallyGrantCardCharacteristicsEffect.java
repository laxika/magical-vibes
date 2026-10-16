package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Records card types, subtypes, and activated abilities that follow a card identity between zones.
 *
 * <p>When {@code cardId} is null the grant applies to the source permanent's card. A hand-choice
 * follow-up (such as a spellbook draft) binds {@code cardId} to the chosen card through
 * {@link ChosenCardAwareEffect}, so the grant lands on that card rather than on its source.
 *
 * <p>When {@code replacesCreatureTypes} is set the granted subtypes are the card's new creature types,
 * replacing the ones it had ("perpetually becomes a Dragon"); otherwise they are added to the existing
 * subtypes ("in addition to its other types").
 */
public record PerpetuallyGrantCardCharacteristicsEffect(
        Set<CardType> cardTypes,
        Set<CardSubtype> subtypes,
        List<ActivatedAbility> activatedAbilities,
        UUID cardId,
        boolean replacesCreatureTypes
) implements CardEffect, ChosenCardAwareEffect {

    public PerpetuallyGrantCardCharacteristicsEffect {
        cardTypes = Set.copyOf(cardTypes);
        subtypes = Set.copyOf(subtypes);
        activatedAbilities = List.copyOf(activatedAbilities);
    }

    public PerpetuallyGrantCardCharacteristicsEffect(Set<CardType> cardTypes, Set<CardSubtype> subtypes,
                                                       List<ActivatedAbility> activatedAbilities, UUID cardId) {
        this(cardTypes, subtypes, activatedAbilities, cardId, false);
    }

    public PerpetuallyGrantCardCharacteristicsEffect(Set<CardType> cardTypes, Set<CardSubtype> subtypes,
                                                       List<ActivatedAbility> activatedAbilities) {
        this(cardTypes, subtypes, activatedAbilities, null, false);
    }

    /** The card perpetually becomes the given creature types, replacing the creature types it had. */
    public static PerpetuallyGrantCardCharacteristicsEffect becomingCreatureTypes(Set<CardSubtype> creatureTypes) {
        return new PerpetuallyGrantCardCharacteristicsEffect(Set.of(), creatureTypes, List.of(), null, true);
    }

    public PerpetuallyGrantCardCharacteristicsEffect(CardType cardType, CardSubtype subtype,
                                                       ActivatedAbility activatedAbility) {
        this(Set.of(cardType), Set.of(subtype), List.of(activatedAbility), null, false);
    }

    @Override
    public CardEffect withChosenCard(Card card) {
        return new PerpetuallyGrantCardCharacteristicsEffect(
                cardTypes, subtypes, activatedAbilities, card.getId(), replacesCreatureTypes);
    }
}
