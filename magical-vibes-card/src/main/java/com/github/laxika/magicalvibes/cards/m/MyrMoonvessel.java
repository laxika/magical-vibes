package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;

@CardRegistration(set = "DST", collectorNumber = "133")
public class MyrMoonvessel extends Card {

    public MyrMoonvessel() {
        addEffect(EffectSlot.ON_DEATH, new AwardManaEffect(ManaColor.COLORLESS));
    }
}
