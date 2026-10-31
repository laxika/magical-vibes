package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.IncreaseOpponentCastCostPerTargetEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostForEachTargetEffect;

@CardRegistration(set = "NEO", collectorNumber = "222")
public class HinataDawnCrowned extends Card {

    public HinataDawnCrowned() {
        addStaticEffect(new ReduceOwnCastCostForEachTargetEffect(1));
        addStaticEffect(new IncreaseOpponentCastCostPerTargetEffect(1));
    }
}
