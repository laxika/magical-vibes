package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.TriggerMode;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "AVR", collectorNumber = "156")
@CardRegistration(set = "DDL", collectorNumber = "2")
public class SomberwaldVigilante extends Card {

    public SomberwaldVigilante() {
        addTrigger(Triggers.becomesBlocked(), TriggerMode.PER_BLOCKER, new DealDamageToTargetCreatureEffect(1));
    }
}
