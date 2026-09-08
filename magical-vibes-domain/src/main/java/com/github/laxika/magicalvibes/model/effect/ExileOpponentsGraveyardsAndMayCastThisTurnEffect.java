package com.github.laxika.magicalvibes.model.effect;

/**
 * Exiles all opponents' graveyards, grants the controller permission to cast the exiled
 * nonland cards this turn using mana of any type, and returns cards still in exile to their
 * owners' graveyards at the next end step (Mnemonic Betrayal).
 */
public record ExileOpponentsGraveyardsAndMayCastThisTurnEffect() implements CardEffect {
}
