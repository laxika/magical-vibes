package com.github.laxika.magicalvibes.model.effect;

/**
 * Returns the dying permanent's card, the stack entry's triggering card, to its owner's battlefield face down,
 * then turns it face up when the card's front face is a creature and turning it face up is allowed.
 */
public record ReturnDyingCreatureToOwnerBattlefieldFaceDownThenTurnFaceUpEffect() implements CardEffect {
}
