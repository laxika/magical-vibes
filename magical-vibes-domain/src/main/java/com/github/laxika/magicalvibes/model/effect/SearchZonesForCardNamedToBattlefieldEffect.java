package com.github.laxika.magicalvibes.model.effect;

/**
 * Searches the controller's graveyard, hand, and/or library for a card with the given name and
 * puts it onto the battlefield under their control. Graveyard and hand matches (public / hidden
 * zones the controller already sees) are taken automatically; only the library search presents an
 * interactive pick and shuffles afterwards. Mirrors
 * {@link SearchLibraryAndOrGraveyardForNamedCardToHandEffect} but also checks the hand and puts the
 * card onto the battlefield instead of into hand (Gate to the Afterlife's God-Pharaoh's Gift tutor).
 */
public record SearchZonesForCardNamedToBattlefieldEffect(
        String cardName
) implements CardEffect {
}
