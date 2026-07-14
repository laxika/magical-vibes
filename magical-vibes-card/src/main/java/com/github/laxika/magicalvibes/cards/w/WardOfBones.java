package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.WardOfBonesEffect;

@CardRegistration(set = "EVE", collectorNumber = "174")
public class WardOfBones extends Card {

    public WardOfBones() {
        addEffect(EffectSlot.STATIC, new WardOfBonesEffect());
    }
}
