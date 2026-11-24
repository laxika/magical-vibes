package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CostModificationScope;
import com.github.laxika.magicalvibes.model.effect.KeywordCost;
import com.github.laxika.magicalvibes.model.effect.ModifyKeywordCostEffect;

@CardRegistration(set = "ODY", collectorNumber = "297")
public class CatalystStone extends Card {

    public CatalystStone() {
        addStaticEffect(new ModifyKeywordCostEffect(KeywordCost.FLASHBACK, -2, CostModificationScope.SELF));
        addStaticEffect(new ModifyKeywordCostEffect(KeywordCost.FLASHBACK, 2, CostModificationScope.OPPONENT));
    }
}
