package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.IncreaseOpponentCastCostPerTargetEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostForEachTargetEffect;

@CardRegistration(set = "NEO", collectorNumber = "222")
public class HinataDawnCrowned extends Card {

    public HinataDawnCrowned() {
        addEffect(EffectSlot.STATIC, new ReduceOwnCastCostForEachTargetEffect(1));
        addEffect(EffectSlot.STATIC, new IncreaseOpponentCastCostPerTargetEffect(1));
    }
}
