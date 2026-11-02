package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfSourceEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MH1", collectorNumber = "58")
public class MistSyndicateNaga extends Card {

    public MistSyndicateNaga() {
        addNinjutsu("{2}{U}");
        addTrigger(Triggers.dealsCombatDamageToPlayer(), new CreateTokenCopyOfSourceEffect());
    }
}
