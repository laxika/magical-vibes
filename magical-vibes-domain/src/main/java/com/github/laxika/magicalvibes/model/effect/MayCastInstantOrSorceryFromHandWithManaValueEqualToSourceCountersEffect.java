package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

/**
 * Offers an instant or sorcery card from the controller's hand for a free cast when its mana
 * value equals the number of the specified counters on the source permanent.
 */
public record MayCastInstantOrSorceryFromHandWithManaValueEqualToSourceCountersEffect(
        CounterType counterType) implements CardEffect {
}
