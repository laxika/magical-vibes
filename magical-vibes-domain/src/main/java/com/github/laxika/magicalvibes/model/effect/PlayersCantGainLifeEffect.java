package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect: no player can gain life.
 * Used by Leyline of Punishment and similar effects (e.g. Erebos, God of the Dead).
 * Life loss and damage still apply normally — only life gain is prevented.
 */
public record PlayersCantGainLifeEffect() implements CardEffect {
}
