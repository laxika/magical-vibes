package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.TriggerMode;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;

@CardRegistration(set = "DTK", collectorNumber = "143")
public class KolaghanAspirant extends Card {

    public KolaghanAspirant() {
        // Whenever this creature becomes blocked by a creature, this creature deals 1 damage to that creature.
        addEffect(EffectSlot.ON_BECOMES_BLOCKED,
                new DealDamageToTargetCreatureEffect(1), TriggerMode.PER_BLOCKER);
    }
}
