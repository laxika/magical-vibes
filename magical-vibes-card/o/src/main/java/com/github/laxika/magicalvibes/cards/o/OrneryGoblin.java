package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.TriggerMode;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "GRN", collectorNumber = "112")
public class OrneryGoblin extends Card {

    public OrneryGoblin() {
        addTrigger(Triggers.blocks(), new DealDamageToTargetCreatureEffect(1));
        addTrigger(Triggers.becomesBlocked(), TriggerMode.PER_BLOCKER, new DealDamageToTargetCreatureEffect(1));
    }
}
