package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CostModificationScope;
import com.github.laxika.magicalvibes.model.effect.KeywordCost;
import com.github.laxika.magicalvibes.model.effect.ModifyKeywordCostEffect;

@CardRegistration(set = "EXO", collectorNumber = "134")
public class MemoryCrystal extends Card {

    public MemoryCrystal() {
        addStaticEffect(new ModifyKeywordCostEffect(KeywordCost.BUYBACK, -2, CostModificationScope.ALL));
    }
}
