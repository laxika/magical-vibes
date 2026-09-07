package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.amount.ControllerLifeTotal;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.Max;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostEffect;

@CardRegistration(set = "SNC", collectorNumber = "94")
public class ShadowOfMortality extends Card {

    public ShadowOfMortality() {
        Max lifeDifference = new Max(
                new Fixed(0),
                new Sum(new Fixed(GameData.STARTING_LIFE_TOTAL),
                        new Scaled(new ControllerLifeTotal(), -1)));
        addEffect(EffectSlot.STATIC, new ReduceOwnCastCostEffect(lifeDifference));
    }
}
