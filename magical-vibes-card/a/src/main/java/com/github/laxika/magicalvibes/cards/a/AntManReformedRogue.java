package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MakeCreatureUnblockableEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MSC", collectorNumber = "739")
public class AntManReformedRogue extends Card {

    public AntManReformedRogue() {
        addTrigger(Triggers.youCastSpell().withFilter(new CardColorPredicate(CardColor.GREEN)),
                new BoostSelfEffect(1, 0),
                new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.SELF));
        addTrigger(Triggers.youCastSpell().withFilter(new CardColorPredicate(CardColor.BLUE)),
                new BoostSelfEffect(-1, 0),
                new MakeCreatureUnblockableEffect(true));
        addTrigger(Triggers.dealsCombatDamageToPlayer(), new DrawCardEffect());
    }
}
