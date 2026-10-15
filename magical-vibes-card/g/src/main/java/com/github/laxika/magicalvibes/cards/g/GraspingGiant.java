package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.TriggerMode;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentUntilSourceLeavesEffect;

@CardRegistration(set = "THB", collectorNumber = "288")
public class GraspingGiant extends Card {

    public GraspingGiant() {
        addEffect(EffectSlot.ON_BECOMES_BLOCKED,
                new ExileTargetPermanentUntilSourceLeavesEffect(), TriggerMode.PER_BLOCKER);
    }
}
