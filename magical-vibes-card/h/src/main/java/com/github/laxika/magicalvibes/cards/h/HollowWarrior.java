package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantAttackOrBlockUnlessTapEffect;

@CardRegistration(set = "PCY", collectorNumber = "138")
public class HollowWarrior extends Card {

    public HollowWarrior() {
        addEffect(EffectSlot.STATIC, new CantAttackOrBlockUnlessTapEffect());
    }
}
