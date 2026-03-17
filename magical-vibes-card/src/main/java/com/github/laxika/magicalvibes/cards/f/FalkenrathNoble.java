package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerLosesLifeAndControllerGainsLifeEffect;

@CardRegistration(set = "ISD", collectorNumber = "100")
public class FalkenrathNoble extends Card {

    public FalkenrathNoble() {
        // Whenever Falkenrath Noble or another creature dies, target player loses 1 life and you gain 1 life.
        addEffect(EffectSlot.ON_DEATH, new TargetPlayerLosesLifeAndControllerGainsLifeEffect(1, 1));
        addEffect(EffectSlot.ON_ANY_CREATURE_DIES, new TargetPlayerLosesLifeAndControllerGainsLifeEffect(1, 1));
    }
}
