package com.github.laxika.magicalvibes.model.effect;

/**
 * Exiles the card chosen from the targeted player's hand by the preceding
 * {@link ChooseCardsFromTargetHandEffect} with {@link HandChoiceDestination#KEEP_IN_HAND}. Lets a
 * spell make its hand choice first and exile the chosen card only after its later choices are made
 * ("choose a card from their hand, then choose a card from their graveyard. Exile the chosen
 * cards"). Does nothing when no hand card was chosen or the card has left the hand.
 */
public record ExileChosenCardFromTargetHandEffect() implements CardEffect {
}
