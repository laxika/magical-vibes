package com.github.laxika.magicalvibes.model.effect;

/**
 * ETB replacement effect: "If this creature was kicked, it enters the battlefield
 * with N +1/+1 counters on it." (e.g. Academy Drake)
 *
 * @param count the number of +1/+1 counters to add when kicked
 */
public record EnterWithPlusOnePlusOneCountersIfKickedEffect(int count) implements ReplacementEffect {
}
