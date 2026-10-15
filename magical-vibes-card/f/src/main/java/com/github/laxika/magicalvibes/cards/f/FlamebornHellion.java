package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MustAttackEffect;

@CardRegistration(set = "SOM", collectorNumber = "89")
public class FlamebornHellion extends Card {

    public FlamebornHellion() {
        addEffect(EffectSlot.STATIC, new MustAttackEffect());
    }
}
