package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "M10", collectorNumber = "142")
public class InfernoElemental extends Card {

    public InfernoElemental() {
        addTrigger(Triggers.blocks(), new DealDamageToTargetCreatureEffect(3));
        addTrigger(Triggers.becomesBlocked(), TriggerOptions.EACH_BLOCKER, new DealDamageToTargetCreatureEffect(3));
    }
}
