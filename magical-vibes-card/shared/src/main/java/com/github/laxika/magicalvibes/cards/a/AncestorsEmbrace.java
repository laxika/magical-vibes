package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ExileInsteadOfGraveyardReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

public class AncestorsEmbrace extends Card {

    public AncestorsEmbrace() {
        target(TargetFilters.creature())
                .addStaticEffect(new GrantKeywordEffect(Keyword.LIFELINK, GrantScope.ENCHANTED_CREATURE))
                .addStaticEffect(new ExileInsteadOfGraveyardReplacementEffect());
    }
}
