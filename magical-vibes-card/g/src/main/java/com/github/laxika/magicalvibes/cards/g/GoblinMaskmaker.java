package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ReduceCastCostForMatchingSpellsUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.filter.CardTruePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MKM", collectorNumber = "130")
public class GoblinMaskmaker extends Card {

    public GoblinMaskmaker() {
        addTrigger(Triggers.attacks(),
                new ReduceCastCostForMatchingSpellsUntilEndOfTurnEffect(new CardTruePredicate(), 1, true));
    }
}
