package com.github.laxika.magicalvibes.model.effect;

/** Static marker for Strict Proctor's triggered-ability counter tax. */
public record StrictProctorEffect(int counterCost) implements TriggeredAbilityCounterEffect {

    public StrictProctorEffect() {
        this(2);
    }
}
