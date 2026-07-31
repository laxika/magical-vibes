package com.github.laxika.magicalvibes.model.effect;

/**
 * Misfortune. An opponent chooses one — either you put a +1/+1 counter on each creature you control
 * and gain 4 life, or you put a -1/-1 counter on each creature that player controls and Misfortune
 * deals 4 damage to that player. The choosing opponent is prompted through the may-ability
 * (accept/decline) system: accept is the counters-and-life mode, decline is the shrink-and-burn mode.
 */
public record MisfortuneEffect() implements CardEffect {
}
