package com.github.laxika.magicalvibes.model.effect;

/**
 * Counters each opponent-controlled spell and activated or triggered ability on the stack unless
 * its controller pays the configured generic mana amount.
 */
public record CounterOpponentsSpellsAndAbilitiesUnlessPaysEffect(int amount)
        implements CounterSpellingEffect {
}
