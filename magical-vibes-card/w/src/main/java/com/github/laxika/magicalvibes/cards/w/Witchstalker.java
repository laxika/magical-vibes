package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "M14", collectorNumber = "202")
public class Witchstalker extends Card {

    public Witchstalker() {
        // Hexproof is auto-loaded from Scryfall.
        // Whenever an opponent casts a blue or black spell during your turn, put a +1/+1 counter
        // on this creature.
        addTrigger(Triggers.opponentCastsSpell()
                        .withFilter(new CardAnyOfPredicate(List.of(
                                new CardColorPredicate(CardColor.BLUE),
                                new CardColorPredicate(CardColor.BLACK))))
                        .duringYourTurn(),
                new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE));
    }
}
