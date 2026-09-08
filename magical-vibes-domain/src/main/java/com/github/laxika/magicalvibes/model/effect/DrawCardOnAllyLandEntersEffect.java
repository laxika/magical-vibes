package com.github.laxika.magicalvibes.model.effect;

/**
 * Marker stored in an emblem's static effects for "Whenever a land you control enters, you may
 * draw a card." The land-entry trigger collector turns it into a resolution-time may ability.
 */
public interface DrawCardOnAllyLandEntersEffect extends CardEffect {

    record Marker() implements DrawCardOnAllyLandEntersEffect {
    }
}
