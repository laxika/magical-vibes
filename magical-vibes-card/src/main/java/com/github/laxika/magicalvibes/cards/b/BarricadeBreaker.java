package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MustAttackEffect;

@CardRegistration(set = "AER", collectorNumber = "144")
public class BarricadeBreaker extends Card {

    public BarricadeBreaker() {
        addEffect(EffectSlot.STATIC, new MustAttackEffect());
    }
}
