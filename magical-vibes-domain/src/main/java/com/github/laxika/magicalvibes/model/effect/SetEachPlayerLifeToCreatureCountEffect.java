package com.github.laxika.magicalvibes.model.effect;

/**
 * Each player's life total becomes the number of creatures they control.
 * Used by Biorhythm.
 */
public record SetEachPlayerLifeToCreatureCountEffect() implements CardEffect {
}
