package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.FirstPowerUpFreeEffect;

@CardRegistration(set = "MSC", collectorNumber = "712")
public class AdvancingTheSpirit extends Card {

    public AdvancingTheSpirit() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DrawCardEffect());
        addEffect(EffectSlot.STATIC, new FirstPowerUpFreeEffect());
    }
}
