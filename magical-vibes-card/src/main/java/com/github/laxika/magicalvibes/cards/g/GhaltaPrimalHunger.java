package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.Max;
import com.github.laxika.magicalvibes.model.amount.TotalPowerOfControlledCreatures;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostEffect;

@CardRegistration(set = "FDN", collectorNumber = "222")
@CardRegistration(set = "RIX", collectorNumber = "130")
public class GhaltaPrimalHunger extends Card {

    public GhaltaPrimalHunger() {
        addEffect(EffectSlot.STATIC, new ReduceOwnCastCostEffect(
                new Max(new Fixed(0), new TotalPowerOfControlledCreatures())));
    }
}
