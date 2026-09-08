package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.XValue;

import java.util.List;
import java.util.Set;

/** Creates a single creature token and puts the evaluated counter amount on it. */
public record CreateXTokenWithXCountersEffect(
        String tokenName,
        int power,
        int toughness,
        CardColor color,
        Set<CardColor> colors,
        List<CardSubtype> subtypes,
        CounterType counterType,
        DynamicAmount counterAmount
) implements CardEffect {

    public CreateXTokenWithXCountersEffect(
            String tokenName,
            int power,
            int toughness,
            CardColor color,
            Set<CardColor> colors,
            List<CardSubtype> subtypes,
            CounterType counterType) {
        this(tokenName, power, toughness, color, colors, subtypes, counterType, new XValue());
    }
}
