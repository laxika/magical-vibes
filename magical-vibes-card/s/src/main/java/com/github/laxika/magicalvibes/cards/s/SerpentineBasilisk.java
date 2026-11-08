package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentAtEndOfCombatEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ONS", collectorNumber = "280")
public class SerpentineBasilisk extends Card {

    public SerpentineBasilisk() {
        addMorph("{1}{G}{G}");

        addTrigger(Triggers.dealsDamageToCreature().withCombatDamageOnly(),
                DestroyTargetPermanentAtEndOfCombatEffect.ofTriggeringPermanent());
    }
}
