package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.Set;

@CardRegistration(set = "OANA", collectorNumber = "1")
@CardRegistration(set = "ANB", collectorNumber = "3")
public class AngelicReward extends Card {

    public AngelicReward() {
        target(TargetFilters.creature())
                .addStaticEffect(new StaticBoostEffect(3, 3, Set.of(Keyword.FLYING), GrantScope.ENCHANTED_CREATURE));
    }
}
