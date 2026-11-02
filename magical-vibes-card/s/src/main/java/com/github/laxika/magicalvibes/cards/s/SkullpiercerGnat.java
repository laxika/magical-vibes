package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyGivePoisonCounterOnCastToRandomNonlandCardInDamagedHandEffect;
import com.github.laxika.magicalvibes.model.effect.ToxicEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "YONE", collectorNumber = "28")
public class SkullpiercerGnat extends Card {

    public SkullpiercerGnat() {
        addStaticEffect(new ToxicEffect(1));
        addTrigger(Triggers.dealsCombatDamageToPlayer(),
                new PerpetuallyGivePoisonCounterOnCastToRandomNonlandCardInDamagedHandEffect());
    }
}
