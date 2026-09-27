package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CounterSpellAndPerpetuallyIncreaseSameNameCastCostEffect;

@CardRegistration(set = "YDMU", collectorNumber = "27")
public class TeferisContingency extends Card {

    public TeferisContingency() {
        addEffect(EffectSlot.SPELL, new CounterSpellAndPerpetuallyIncreaseSameNameCastCostEffect(2));
    }
}
