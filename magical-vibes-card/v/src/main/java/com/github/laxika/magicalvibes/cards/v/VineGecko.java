package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceCastCostForFirstMatchingSpellEachTurnEffect;
import com.github.laxika.magicalvibes.model.filter.CardTruePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ZNR", collectorNumber = "219")
public class VineGecko extends Card {

    public VineGecko() {
        addStaticEffect(new ReduceCastCostForFirstMatchingSpellEachTurnEffect(
                new CardTruePredicate(), 1, true));
        addTrigger(Triggers.youCastSpell().kicked(), new PutCountersOnSourceEffect(1, 1, 1));
    }
}
