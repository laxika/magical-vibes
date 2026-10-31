package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "9ED", collectorNumber = "281")
@CardRegistration(set = "4ED", collectorNumber = "287")
@CardRegistration(set = "3ED", collectorNumber = "229")
@CardRegistration(set = "LEA", collectorNumber = "228")
@CardRegistration(set = "LEB", collectorNumber = "229")
@CardRegistration(set = "SUM", collectorNumber = "229")
@CardRegistration(set = "2ED", collectorNumber = "229")
public class Web extends Card {

    public Web() {
        target(TargetFilters.creature()).addStaticEffect(new StaticBoostEffect(0, 2, GrantScope.ENCHANTED_CREATURE))
                .addStaticEffect(new GrantKeywordEffect(Keyword.REACH, GrantScope.ENCHANTED_CREATURE));
    }
}
