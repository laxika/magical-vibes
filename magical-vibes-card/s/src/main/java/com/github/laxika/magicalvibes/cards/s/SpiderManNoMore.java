package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.LosesAllAbilitiesEffect;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "SPM", collectorNumber = "45")
@CardRegistration(set = "OM1", collectorNumber = "31")
public class SpiderManNoMore extends Card {

    public SpiderManNoMore() {
        target(TargetFilters.creature())
                .addStaticEffect(new GrantSubtypeEffect(CardSubtype.CITIZEN, GrantScope.ENCHANTED_CREATURE, true))
                .addStaticEffect(new SetBasePowerToughnessEffect(1, 1, GrantScope.ENCHANTED_CREATURE))
                .addStaticEffect(new GrantKeywordEffect(Keyword.DEFENDER, GrantScope.ENCHANTED_CREATURE))
                .addStaticEffect(new LosesAllAbilitiesEffect(GrantScope.ENCHANTED_CREATURE));
    }
}
