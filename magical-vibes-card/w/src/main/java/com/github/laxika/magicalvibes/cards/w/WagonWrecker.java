package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyGiveSpellCastLifeLossToRandomNonlandCardInDamagedHandEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "YOTJ", collectorNumber = "15")
public class WagonWrecker extends Card {

    public WagonWrecker() {
        addTrigger(Triggers.dealsCombatDamageToPlayer(),
                new PerpetuallyGiveSpellCastLifeLossToRandomNonlandCardInDamagedHandEffect());
    }
}
