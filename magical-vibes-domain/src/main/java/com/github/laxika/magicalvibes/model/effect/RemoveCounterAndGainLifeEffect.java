package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

/**
 * "Remove a {counter} counter from {this permanent | target creature}. If you do, you gain
 * {lifeGain} life." Removes a single counter of the given type from the {@code subject}; the
 * controller only gains life if a counter was actually removed ("If you do").
 *
 * <p>{@link CounterRemovalSubject#SOURCE} is non-targeting — Living Artifact ({@code VITALITY}, 1,
 * wrapped in a {@link MayEffect} on its upkeep trigger) and Exemplar of Strength
 * ({@code MINUS_ONE_MINUS_ONE}, 1, on attack). {@link CounterRemovalSubject#TARGET} targets a
 * creature — Woeleecher ({@code MINUS_ONE_MINUS_ONE}, 2) and Chainbreaker
 * ({@code MINUS_ONE_MINUS_ONE}, 0, which removes a counter and gains no life).</p>
 */
public record RemoveCounterAndGainLifeEffect(CounterType counterType,
                                            int lifeGain,
                                            CounterRemovalSubject subject) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return subject == CounterRemovalSubject.TARGET
                ? TargetSpec.benign(TargetPredicates.creature())
                : new TargetSpec(null, false, null, true, 1);
    }
}
