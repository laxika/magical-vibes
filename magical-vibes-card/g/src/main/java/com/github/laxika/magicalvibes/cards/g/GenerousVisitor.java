package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "NEO", collectorNumber = "185")
public class GenerousVisitor extends Card {

    public GenerousVisitor() {
        target(TargetFilters.creature()).addTrigger(Triggers.youCastSpell()
                        .withFilter(new CardTypePredicate(CardType.ENCHANTMENT)),
                new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE));
    }
}
