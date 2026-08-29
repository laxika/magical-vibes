package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

/**
 * Static replacement effect: prevent damage to this permanent's controller and put a counter on
 * the permanent carrying this effect.
 */
public record PreventDamageToControllerAndPutCounterOnSelfEffect(CounterType counterType) implements CardEffect {
}
