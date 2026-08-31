package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

/**
 * Static self-effect: the source gains the activated abilities of each other creature carrying
 * at least one counter of the specified type.
 */
public record GainActivatedAbilitiesOfCreaturesWithCounterEffect(CounterType counterType)
        implements CardEffect {
}
