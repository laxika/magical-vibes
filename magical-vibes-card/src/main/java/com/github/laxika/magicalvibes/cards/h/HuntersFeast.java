package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EachTargetPlayerGainsLifeEffect;

@CardRegistration(set = "M11", collectorNumber = "182")
public class HuntersFeast extends Card {

    public HuntersFeast() {
        setMinTargets(0);
        setMaxTargets(99);
        addEffect(EffectSlot.SPELL, new EachTargetPlayerGainsLifeEffect(6));
    }
}
