package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "EXO", collectorNumber = "92")
public class Onslaught extends Card {

    public Onslaught() {
        // Whenever you cast a creature spell, tap target creature.
        target(TargetFilters.creature()).addTrigger(Triggers.youCastSpell()
                        .withFilter(new CardTypePredicate(CardType.CREATURE)),
                new TapPermanentsEffect(TapUntapScope.TARGET));
    }
}
