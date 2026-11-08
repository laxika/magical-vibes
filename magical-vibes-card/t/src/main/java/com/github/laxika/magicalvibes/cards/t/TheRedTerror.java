package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceCardEffect;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.Set;

@CardRegistration(set = "40K", collectorNumber = "83")
public class TheRedTerror extends Card {

    public TheRedTerror() {
        addTrigger(Triggers.sourceYouControlDealsDamage()
                        .withFilter(new PermanentColorInPredicate(Set.of(CardColor.RED)))
                        .withSpellFilter(new CardColorPredicate(CardColor.RED)),
                new PutCountersOnSourceCardEffect(CounterType.PLUS_ONE_PLUS_ONE));
    }
}
