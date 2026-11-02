package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.GivePoisonCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PoisonRecipient;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

public class EtaliPrimalSickness extends Card {

    public EtaliPrimalSickness() {
        addTrigger(Triggers.dealsCombatDamageToPlayer(),
                new GivePoisonCountersEffect(new EventValue(), PoisonRecipient.TARGET_PLAYER));
    }
}
