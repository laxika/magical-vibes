package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MaximumCombatCreaturesEffect;

@CardRegistration(set = "5DN", collectorNumber = "150")
public class SilentArbiter extends Card {

    public SilentArbiter() {
        addEffect(EffectSlot.STATIC, new MaximumCombatCreaturesEffect(1, 1));
    }
}
