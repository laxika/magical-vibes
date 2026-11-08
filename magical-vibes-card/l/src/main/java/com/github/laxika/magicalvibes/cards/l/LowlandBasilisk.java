package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentAtEndOfCombatEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "STH", collectorNumber = "109")
@CardRegistration(set = "TPR", collectorNumber = "178")
public class LowlandBasilisk extends Card {

    public LowlandBasilisk() {
        addTrigger(Triggers.dealsDamageToCreature(),
                DestroyTargetPermanentAtEndOfCombatEffect.ofTriggeringPermanent());
    }
}
