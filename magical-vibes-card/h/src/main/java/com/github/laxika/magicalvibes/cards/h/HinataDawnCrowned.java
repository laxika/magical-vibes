package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CostModificationScope;
import com.github.laxika.magicalvibes.model.effect.ModifyCastCostPerTargetEffect;

@CardRegistration(set = "NEO", collectorNumber = "222")
public class HinataDawnCrowned extends Card {

    public HinataDawnCrowned() {
        addStaticEffect(new ModifyCastCostPerTargetEffect(-1, CostModificationScope.SELF, null, null));
        addStaticEffect(new ModifyCastCostPerTargetEffect(1, CostModificationScope.OPPONENT, null, null));
    }
}
