package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.k.KenzoTheHardhearted;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.TransformToBackFaceEffect;

@CardRegistration(set = "CHK", collectorNumber = "2")
public class BushiTenderfoot extends Card {

    public BushiTenderfoot() {
        setBackFaceCard(new KenzoTheHardhearted());
        addEffect(EffectSlot.ON_DAMAGED_CREATURE_DIES, new TransformToBackFaceEffect());
    }

    @Override
    public String getBackFaceClassName() {
        return "KenzoTheHardhearted";
    }
}
