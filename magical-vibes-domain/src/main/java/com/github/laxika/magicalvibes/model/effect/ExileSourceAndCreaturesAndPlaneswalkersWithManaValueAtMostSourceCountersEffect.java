package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

/**
 * Exiles the source permanent, all battlefield creatures and planeswalkers, and all creature and
 * planeswalker cards in graveyards whose mana value is at most the source's counter count.
 */
public record ExileSourceAndCreaturesAndPlaneswalkersWithManaValueAtMostSourceCountersEffect(
        CounterType counterType) implements CardEffect {
}
