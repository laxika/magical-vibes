package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.XValue;

import java.util.List;
import java.util.Set;

/** Creates one token from a template and puts a dynamic number of counters on it. */
public record CreateXTokenWithXCountersEffect(
        CreateTokenEffect tokenTemplate,
        DynamicAmount counterAmount,
        CounterType counterType
) implements CardEffect {

    public CreateXTokenWithXCountersEffect(String tokenName, int power, int toughness,
                                           CardColor color, Set<CardColor> colors,
                                           List<CardSubtype> subtypes, CounterType counterType) {
        this(new CreateTokenEffect(tokenName, power, toughness, color, colors, subtypes),
                new XValue(), counterType);
    }
}
