package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "GRN", collectorNumber = "112")
public class OrneryGoblin extends Card {

    public OrneryGoblin() {
        addTrigger(Triggers.blocks(), DealDamageEffect.toTargetCreature(1));
        addTrigger(Triggers.becomesBlocked(), TriggerOptions.EACH_BLOCKER, DealDamageEffect.toTargetCreature(1));
    }
}
