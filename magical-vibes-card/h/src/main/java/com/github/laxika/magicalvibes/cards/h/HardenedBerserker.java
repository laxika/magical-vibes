package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ReduceCastCostForNextMatchingSpellEffect;
import com.github.laxika.magicalvibes.model.filter.CardTruePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DTK", collectorNumber = "139")
public class HardenedBerserker extends Card {

    public HardenedBerserker() {
        addTrigger(Triggers.attacks(),
                new ReduceCastCostForNextMatchingSpellEffect(new CardTruePredicate(), 1));
    }
}
