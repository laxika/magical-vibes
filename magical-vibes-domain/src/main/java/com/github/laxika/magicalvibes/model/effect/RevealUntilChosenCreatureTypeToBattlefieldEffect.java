package com.github.laxika.magicalvibes.model.effect;

/**
 * Chooses a creature type, then reveals cards from the controller's library until a creature card
 * of that type is revealed. The matching card is put onto the battlefield and the other revealed
 * cards are shuffled into the library.
 *
 * <p>Changeling cards match every chosen creature type.</p>
 */
public record RevealUntilChosenCreatureTypeToBattlefieldEffect() implements CardEffect {
}
