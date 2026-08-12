package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MustAttackEffect;

@CardRegistration(set = "M20", collectorNumber = "334")
public class RubblebeltRecluse extends Card {

    public RubblebeltRecluse() {
        addEffect(EffectSlot.STATIC, new MustAttackEffect());
    }
}
