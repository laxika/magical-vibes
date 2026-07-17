package com.github.laxika.magicalvibes.model.effect;

/**
 * The player with strictly the most life among all players gains control of the source creature
 * (Ghazbán Ogre). If two or more players are tied for the most life, no one gains control.
 */
public record PlayerWithMostLifeGainsControlOfSourceCreatureEffect() implements CardEffect {
}
