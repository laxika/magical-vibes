package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutAwakeningCountersOnTargetLandsEffect;

@CardRegistration(set = "SOM", collectorNumber = "123")
public class LiegeOfTheTangle extends Card {

    public LiegeOfTheTangle() {
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, new PutAwakeningCountersOnTargetLandsEffect());
    }
}
