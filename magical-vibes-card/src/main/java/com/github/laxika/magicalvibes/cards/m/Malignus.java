package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.HalvedRoundedUp;
import com.github.laxika.magicalvibes.model.amount.HighestOpponentLifeTotal;
import com.github.laxika.magicalvibes.model.effect.SetPowerToughnessToAmountEffect;
import com.github.laxika.magicalvibes.model.effect.SourceDamageCantBePreventedEffect;

@CardRegistration(set = "AVR", collectorNumber = "148")
public class Malignus extends Card {

    public Malignus() {
        DynamicAmount halfHighestOpponentLife = new HalvedRoundedUp(new HighestOpponentLifeTotal());
        addEffect(EffectSlot.STATIC, new SetPowerToughnessToAmountEffect(halfHighestOpponentLife, halfHighestOpponentLife));
        addEffect(EffectSlot.STATIC, new SourceDamageCantBePreventedEffect());
    }
}
