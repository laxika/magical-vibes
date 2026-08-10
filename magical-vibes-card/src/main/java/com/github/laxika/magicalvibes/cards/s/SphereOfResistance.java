package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CostModificationScope;
import com.github.laxika.magicalvibes.model.effect.IncreaseSpellCostEffect;
import com.github.laxika.magicalvibes.model.filter.CardTruePredicate;

@CardRegistration(set = "EXO", collectorNumber = "139")
public class SphereOfResistance extends Card {

    public SphereOfResistance() {
        addEffect(EffectSlot.STATIC, new IncreaseSpellCostEffect(
                new CardTruePredicate(), 1, CostModificationScope.ALL));
    }
}
