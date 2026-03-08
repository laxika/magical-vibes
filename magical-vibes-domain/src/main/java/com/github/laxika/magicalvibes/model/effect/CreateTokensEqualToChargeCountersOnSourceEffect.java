package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;

import java.util.List;
import java.util.Set;

/**
 * Creates creature tokens equal to the number of charge counters on the source permanent.
 * The count is snapshotted as xValue before sacrifice (see ActivatedAbilityExecutionService).
 */
public record CreateTokensEqualToChargeCountersOnSourceEffect(
        String tokenName,
        int power,
        int toughness,
        CardColor color,
        List<CardSubtype> subtypes,
        Set<Keyword> keywords,
        Set<CardType> additionalTypes
) implements CardEffect {
}
