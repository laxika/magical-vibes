package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MustAttackEffect;

@CardRegistration(set = "INV", collectorNumber = "283")
public class UrborgDrake extends Card {

    public UrborgDrake() {
        addEffect(EffectSlot.STATIC, new MustAttackEffect());
    }
}
