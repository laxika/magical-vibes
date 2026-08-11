package com.github.laxika.magicalvibes.model.effect;

/**
 * Replacement behavior for +1/+1 counters put on creatures controlled by the effect's controller.
 */
public interface PlusOnePlusOneCountersReplacementEffect extends CardEffect {

    int replace(int count);
}
