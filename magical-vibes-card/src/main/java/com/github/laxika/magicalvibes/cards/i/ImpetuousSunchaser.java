package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MustAttackEffect;

@CardRegistration(set = "BNG", collectorNumber = "99")
public class ImpetuousSunchaser extends Card {

    public ImpetuousSunchaser() {
        addEffect(EffectSlot.STATIC, new MustAttackEffect());
    }
}
