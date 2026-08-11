package com.github.laxika.magicalvibes.model.effect;

import java.util.Objects;

/**
 * Counters opponent-controlled spells and activated or triggered abilities on the stack, then
 * creates one token from {@code tokenTemplate} for each spell or ability successfully countered.
 */
public record CounterOpponentsSpellsAndAbilitiesEffect(CreateTokenEffect tokenTemplate)
        implements CounterSpellingEffect {

    public CounterOpponentsSpellsAndAbilitiesEffect {
        Objects.requireNonNull(tokenTemplate, "tokenTemplate is required");
    }
}
