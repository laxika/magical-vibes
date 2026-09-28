package com.github.laxika.magicalvibes.model.effect;

/** The controller wins if the current stack entry's event value equals the expected value. */
public record WinGameIfEventValueEqualsEffect(int expectedValue) implements CardEffect {
}
