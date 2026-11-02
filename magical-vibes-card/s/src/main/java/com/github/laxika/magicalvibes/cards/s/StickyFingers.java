package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenForTriggeringPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SNC", collectorNumber = "124")
@CardRegistration(set = "PIP", collectorNumber = "191")
@CardRegistration(set = "PIP", collectorNumber = "719")
public class StickyFingers extends Card {

    public StickyFingers() {
        target(TargetFilters.creature())
                .addStaticEffect(new GrantKeywordEffect(Keyword.MENACE, GrantScope.ENCHANTED_CREATURE))
                .addTrigger(Triggers.dealsCombatDamageToPlayer(),
                        new CreateTokenForTriggeringPlayerEffect(CreateTokenEffect.ofTreasureToken(1)))
                .addTrigger(Triggers.dies(ObjectRelation.ENCHANTED), new DrawCardEffect());
    }
}
