package com.github.laxika.magicalvibes.model.effect;

/**
 * Chooses a creature type, then reveals cards from the controller's library until a number of
 * creature cards of that type equal to the controller's creature count of that type have been
 * revealed. The matching cards are put onto the battlefield and the other revealed cards are
 * shuffled into the library.
 *
 * <p>Changeling cards match every chosen creature type.</p>
 */
public record RevealUntilChosenCreatureTypeCountToBattlefieldEffect() implements CardEffect {
}
