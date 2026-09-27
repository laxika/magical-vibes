package com.github.laxika.magicalvibes.model.effect;

/**
 * Exiles the controller's top library card. A land is put onto the battlefield; otherwise the
 * controller may cast the exiled card without paying its mana cost.
 */
public record ExileTopCardPutLandOntoBattlefieldOrMayCastFreeEffect() implements CardEffect {
}
