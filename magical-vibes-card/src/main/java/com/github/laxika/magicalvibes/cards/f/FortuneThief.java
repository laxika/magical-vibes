package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DamageLifeFloorEffect;
import com.github.laxika.magicalvibes.model.effect.LifeFloorCondition;

@CardRegistration(set = "TSP", collectorNumber = "156")
public class FortuneThief extends Card {

    public FortuneThief() {
        addMorph("{R}{R}");
        addEffect(EffectSlot.STATIC, new DamageLifeFloorEffect(1, LifeFloorCondition.ALWAYS));
    }
}
