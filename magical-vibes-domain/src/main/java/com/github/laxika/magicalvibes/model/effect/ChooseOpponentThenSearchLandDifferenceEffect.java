package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;

/**
 * Chooses an opponent who controls more lands than the controller, then searches for a number of
 * cards with the given land subtype equal to the land-count difference. The first card enters the
 * battlefield tapped and the remaining cards go to hand.
 */
public record ChooseOpponentThenSearchLandDifferenceEffect(CardSubtype subtype) implements CardEffect {}
