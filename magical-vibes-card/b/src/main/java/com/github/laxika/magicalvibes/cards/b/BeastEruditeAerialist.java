package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentReceivedPlusOnePlusOneCounterThisTurnPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MSH", collectorNumber = "206")
public class BeastEruditeAerialist extends Card {

    public BeastEruditeAerialist() {
        addStaticEffect(new GrantKeywordEffect(
                Keyword.FLYING,
                GrantScope.SELF,
                new PermanentReceivedPlusOnePlusOneCounterThisTurnPredicate(true)));
        addTrigger(Triggers.dealsCombatDamageToPlayer(), new DrawCardEffect(1));
    }
}
