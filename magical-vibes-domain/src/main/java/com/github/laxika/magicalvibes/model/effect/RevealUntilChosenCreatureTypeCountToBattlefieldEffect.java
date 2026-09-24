package com.github.laxika.magicalvibes.model.effect;

/**
 * Chooses a creature type, counts the controller's creatures of that type, then reveals cards
 * from the top of that player's library until that many matching creature cards are revealed.
 * Every matching card is put onto the battlefield and the other revealed cards are shuffled into
 * the library. Changeling cards match every chosen creature type.
 */
public record RevealUntilChosenCreatureTypeCountToBattlefieldEffect() implements CardEffect {
}
