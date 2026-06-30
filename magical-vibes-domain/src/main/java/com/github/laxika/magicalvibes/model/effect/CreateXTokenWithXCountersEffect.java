package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;

import java.util.List;
import java.util.Set;

/**
 * Creates a single creature token and puts X counters of the given type on it, where X comes from
 * the spell or activated ability's X value ({@code StackEntry.getXValue()}).
 */
public record CreateXTokenWithXCountersEffect(
        String tokenName,
        int power,
        int toughness,
        CardColor color,
        Set<CardColor> colors,
        List<CardSubtype> subtypes,
        CounterType counterType
) implements CardEffect {
}
