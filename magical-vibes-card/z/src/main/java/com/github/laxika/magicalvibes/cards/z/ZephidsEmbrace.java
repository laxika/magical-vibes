package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.Set;

@CardRegistration(set = "USG", collectorNumber = "114")
public class ZephidsEmbrace extends Card {

    public ZephidsEmbrace() {
        target(TargetFilters.creature())
                .addStaticEffect(new StaticBoostEffect(2, 2, GrantScope.ENCHANTED_CREATURE))
                .addStaticEffect(new GrantKeywordEffect(
                        Set.of(Keyword.FLYING, Keyword.SHROUD),
                        GrantScope.ENCHANTED_CREATURE));
    }
}
