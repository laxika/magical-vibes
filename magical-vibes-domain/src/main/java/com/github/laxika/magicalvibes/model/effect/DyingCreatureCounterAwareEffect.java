package com.github.laxika.magicalvibes.model.effect;

/**
 * Capability for an ally-creature-death effect that needs the dying creature's total counter
 * count. The death trigger collector binds the count before the effect is put on the stack.
 */
public interface DyingCreatureCounterAwareEffect {

    /** Returns the effect with the dying creature's counter count bound in. */
    CardEffect boundToDyingCreatureCounterCount(int counterCount);
}
