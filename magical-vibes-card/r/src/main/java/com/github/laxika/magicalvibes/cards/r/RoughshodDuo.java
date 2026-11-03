package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "BLB", collectorNumber = "150")
public class RoughshodDuo extends Card {

    public RoughshodDuo() {
        target(TargetFilters.creatureYouControl()).addTrigger(Triggers.youCastSpell().expend(4),
                new BoostTargetCreatureEffect(1, 1),
                new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.TARGET));
    }
}
