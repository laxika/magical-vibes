package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CostModificationScope;
import com.github.laxika.magicalvibes.model.effect.IncreaseSpellCostEffect;
import com.github.laxika.magicalvibes.model.filter.CardTruePredicate;

@CardRegistration(set = "EXO", collectorNumber = "139")
@CardRegistration(set = "VMA", collectorNumber = "284")
@CardRegistration(set = "MPS", collectorNumber = "47")
@CardRegistration(set = "SLD", collectorNumber = "1484")
public class SphereOfResistance extends Card {

    public SphereOfResistance() {
        addStaticEffect(new IncreaseSpellCostEffect(
                new CardTruePredicate(), 1, CostModificationScope.ALL));
    }
}
