package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;

import java.util.Set;

/**
 * Exiles up to one card of each card type from the defending player's graveyard, chosen on
 * resolution (the ability does not target), and puts a +1/+1 counter on the source for each card
 * exiled. The chosen cards must be assignable to distinct card-type slots.
 */
public record ExileUpToOneOfEachCardTypeFromGraveyardEffect() implements CardEffect {

    private static final Set<CardType> CARD_TYPE_SLOTS = Set.of(
            CardType.ARTIFACT,
            CardType.BATTLE,
            CardType.CREATURE,
            CardType.ENCHANTMENT,
            CardType.INSTANT,
            CardType.KINDRED,
            CardType.LAND,
            CardType.PLANESWALKER,
            CardType.SORCERY);

    /** The card types of which at most one card may be chosen. */
    public Set<CardType> cardTypeSlots() {
        return CARD_TYPE_SLOTS;
    }
}
