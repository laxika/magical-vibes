package com.github.laxika.magicalvibes.model.effect;

/**
 * Reveals the top card of the controller's library. A nonland card may be cast by paying the
 * configured mana cost, while a land card is put directly onto the battlefield.
 */
public record RevealTopCardMayCastForManaOrPutLandOntoBattlefieldEffect(String manaCost)
        implements CardEffect {
}
