package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GivePoisonCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PoisonRecipient;

@CardRegistration(set = "SOM", collectorNumber = "67")
public class IchorRats extends Card {

    public IchorRats() {
        // When Ichor Rats enters the battlefield, each player gets a poison counter.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new GivePoisonCountersEffect(1, PoisonRecipient.EACH_PLAYER));
    }
}
