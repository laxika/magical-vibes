package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MustAttackEffect;

@CardRegistration(set = "DIS", collectorNumber = "76")
public class UtvaraScalper extends Card {

    public UtvaraScalper() {
        addEffect(EffectSlot.STATIC, new MustAttackEffect());
    }
}
