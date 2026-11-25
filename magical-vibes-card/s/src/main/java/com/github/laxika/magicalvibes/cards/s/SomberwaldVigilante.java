package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "AVR", collectorNumber = "156")
@CardRegistration(set = "DDL", collectorNumber = "2")
public class SomberwaldVigilante extends Card {

    public SomberwaldVigilante() {
        addTrigger(Triggers.becomesBlocked(), TriggerOptions.EACH_BLOCKER, DealDamageEffect.toTargetCreature(1));
    }
}
