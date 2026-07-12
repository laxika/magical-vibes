package com.github.laxika.magicalvibes.model.effect;

/**
 * The controller of this spell/ability takes {@code count} extra turn(s) after this one.
 * When {@code skipUntapStep} is true, each granted extra turn skips its untap step (Savor the Moment).
 */
public record ControllerExtraTurnEffect(int count, boolean skipUntapStep) implements CardEffect {

    public ControllerExtraTurnEffect(int count) {
        this(count, false);
    }
}
