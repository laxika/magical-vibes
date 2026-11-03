package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DTK", collectorNumber = "143")
public class KolaghanAspirant extends Card {

    public KolaghanAspirant() {
        // Whenever this creature becomes blocked by a creature, this creature deals 1 damage to that creature.
        addTrigger(Triggers.becomesBlocked(), TriggerOptions.EACH_BLOCKER, new DealDamageToTargetCreatureEffect(1));
    }
}
