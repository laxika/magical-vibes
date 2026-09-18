package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DamageLifeFloorEffect;
import com.github.laxika.magicalvibes.model.effect.LifeFloorCondition;

@CardRegistration(set = "ME4", collectorNumber = "107")
@CardRegistration(set = "ARN", collectorNumber = "36")
public class AliFromCairo extends Card {

    public AliFromCairo() {
        addEffect(EffectSlot.STATIC, new DamageLifeFloorEffect(1, LifeFloorCondition.ALWAYS));
    }
}
