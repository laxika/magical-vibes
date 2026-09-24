package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

import java.util.Set;

/** Chooses a matching card in the controller's hand and permanently grants it keywords. */
public record ChooseCardFromHandAndApplyPerpetualKeywordEffect(
        CardPredicate cardFilter, Set<Keyword> keywords) implements CardEffect {

    public ChooseCardFromHandAndApplyPerpetualKeywordEffect {
        keywords = Set.copyOf(keywords);
    }
}
