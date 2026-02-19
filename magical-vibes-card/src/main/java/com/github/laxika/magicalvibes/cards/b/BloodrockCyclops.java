package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MustAttackEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "10E", collectorNumber = "192")
public class BloodrockCyclops extends Card {

    public BloodrockCyclops() {
        addEffect(EffectSlot.STATIC, new MustAttackEffect());
    }
}
