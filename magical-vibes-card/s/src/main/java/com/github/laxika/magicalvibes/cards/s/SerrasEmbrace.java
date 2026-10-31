package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "10E", collectorNumber = "40")
@CardRegistration(set = "7ED", collectorNumber = "43")
@CardRegistration(set = "USG", collectorNumber = "47")
@CardRegistration(set = "DDC", collectorNumber = "21")
@CardRegistration(set = "DVD", collectorNumber = "21")
public class SerrasEmbrace extends Card {

    public SerrasEmbrace() {
        target(TargetFilters.creature()).addStaticEffect(new StaticBoostEffect(2, 2, GrantScope.ENCHANTED_CREATURE))
                .addStaticEffect(new GrantKeywordEffect(Keyword.FLYING, GrantScope.ENCHANTED_CREATURE))
                .addStaticEffect(new GrantKeywordEffect(Keyword.VIGILANCE, GrantScope.ENCHANTED_CREATURE));
    }
}
