package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

import java.util.List;

/**
 * "You may pay {@code costPerCounter} for each {@code counterType} counter on this permanent. If you
 * don't, [elseEffects]." The generic sibling of {@link DestroyUnlessPaysPerCounterEffect}, whose
 * fallback is fixed to destroying or sacrificing the source.
 *
 * <p>With zero counters on the source the cost is empty and nothing happens (no prompt, no
 * fallback). The fallback effects are resolved through the {@link ForcedCostOrElseEffect} plumbing,
 * so each of them must be one of the shapes that path supports.</p>
 *
 * @param counterType    counter kind that scales the payment (e.g. {@link CounterType#WAGE})
 * @param costPerCounter mana cost paid once per counter (e.g. {@code "{2}"})
 * @param elseEffects    what happens when the controller doesn't pay
 */
public record PayPerCounterOrElseEffect(CounterType counterType, String costPerCounter, List<CardEffect> elseEffects)
        implements CardEffect {
}
