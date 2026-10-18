package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.XValue;

import java.util.List;
import java.util.Set;

/**
 * Creates one token from a template and puts a dynamic number of counters on it. When
 * {@code entersWithCounters} is set ("the token enters with X +1/+1 counters on it") the amount is
 * evaluated before the token exists and the counters are on it as it enters, so enters-the-battlefield
 * triggers that look at its power see them; only {@link CounterType#PLUS_ONE_PLUS_ONE} is supported.
 */
public record CreateXTokenWithXCountersEffect(
        CreateTokenEffect tokenTemplate,
        DynamicAmount counterAmount,
        CounterType counterType,
        boolean entersWithCounters
) implements TriggeringSpellManaValueEffect {

    public CreateXTokenWithXCountersEffect(CreateTokenEffect tokenTemplate, DynamicAmount counterAmount,
                                           CounterType counterType) {
        this(tokenTemplate, counterAmount, counterType, false);
    }

    public CreateXTokenWithXCountersEffect(String tokenName, int power, int toughness,
                                           CardColor color, Set<CardColor> colors,
                                           List<CardSubtype> subtypes, CounterType counterType, DynamicAmount counterAmount) {
        this(new CreateTokenEffect(tokenName, power, toughness, color, colors, subtypes), counterAmount, counterType);
    }

    public CreateXTokenWithXCountersEffect(String tokenName, int power, int toughness,
                                           CardColor color, Set<CardColor> colors,
                                           List<CardSubtype> subtypes, CounterType counterType) {
        this(new CreateTokenEffect(tokenName, power, toughness, color, colors, subtypes),
                new XValue(), counterType);
    }
}
