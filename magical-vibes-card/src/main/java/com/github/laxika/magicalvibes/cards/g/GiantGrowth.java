package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;

@CardRegistration(set = "10E", collectorNumber = "266")
public class GiantGrowth extends Card {

    public GiantGrowth() {
        setNeedsTarget(true);
        addEffect(EffectSlot.SPELL, new BoostTargetCreatureEffect(3, 3));
    }
}
