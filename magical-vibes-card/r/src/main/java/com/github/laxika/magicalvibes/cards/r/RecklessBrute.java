package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MustAttackEffect;

@CardRegistration(set = "M13", collectorNumber = "144")
public class RecklessBrute extends Card {

    public RecklessBrute() {
        addEffect(EffectSlot.STATIC, new MustAttackEffect());
    }
}
