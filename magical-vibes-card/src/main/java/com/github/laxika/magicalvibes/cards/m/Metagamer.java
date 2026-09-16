package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CostModificationScope;
import com.github.laxika.magicalvibes.model.effect.IncreaseSpellCostEffect;
import com.github.laxika.magicalvibes.model.filter.CardTruePredicate;

@CardRegistration(set = "MB1", collectorNumber = "8")
public class Metagamer extends Card {

    public Metagamer() {
        // The engine has no tournament-result context, so the winning-deck card pool is modeled
        // as all spells.
        addEffect(EffectSlot.STATIC, new IncreaseSpellCostEffect(
                new CardTruePredicate(), 1, CostModificationScope.ALL));
    }
}
