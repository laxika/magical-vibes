package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReduceCastCostForNextMatchingSpellEffect;
import com.github.laxika.magicalvibes.model.filter.CardTruePredicate;

@CardRegistration(set = "DTK", collectorNumber = "139")
public class HardenedBerserker extends Card {

    public HardenedBerserker() {
        addEffect(EffectSlot.ON_ATTACK,
                new ReduceCastCostForNextMatchingSpellEffect(new CardTruePredicate(), 1));
    }
}
