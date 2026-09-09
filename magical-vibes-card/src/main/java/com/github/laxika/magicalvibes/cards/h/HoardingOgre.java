package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.RollD20Effect;

@CardRegistration(set = "AFR", collectorNumber = "146")
public class HoardingOgre extends Card {

    public HoardingOgre() {
        addEffect(EffectSlot.ON_ATTACK, new RollD20Effect(
                CreateTokenEffect.ofTreasureToken(1),
                CreateTokenEffect.ofTreasureToken(2),
                CreateTokenEffect.ofTreasureToken(3)
        ));
    }
}
