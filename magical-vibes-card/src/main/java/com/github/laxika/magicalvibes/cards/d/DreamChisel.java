package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CostModificationScope;
import com.github.laxika.magicalvibes.model.effect.ReduceCastCostForMatchingSpellsEffect;
import com.github.laxika.magicalvibes.model.filter.CardTruePredicate;

@CardRegistration(set = "ONS", collectorNumber = "308")
public class DreamChisel extends Card {

    public DreamChisel() {
        addEffect(EffectSlot.STATIC, new ReduceCastCostForMatchingSpellsEffect(
                new CardTruePredicate(), 1, CostModificationScope.SELF, true));
    }
}
