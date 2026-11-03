package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.NoMaximumHandSizeEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SOC", collectorNumber = "191")
@CardRegistration(set = "C21", collectorNumber = "24")
@CardRegistration(set = "BLC", collectorNumber = "165")
public class CuriosityCrafter extends Card {

    public CuriosityCrafter() {
        addStaticEffect(new NoMaximumHandSizeEffect());
        addTrigger(Triggers.creatureYouControlDealsCombatDamageToPlayer() .withFilter(new PermanentIsTokenPredicate()),
                new DrawCardEffect(1));
    }
}
