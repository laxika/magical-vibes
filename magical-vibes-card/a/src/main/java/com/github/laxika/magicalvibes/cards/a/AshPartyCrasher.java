package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.condition.PermanentEnteredThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "WOE", collectorNumber = "201")
public class AshPartyCrasher extends Card {

    public AshPartyCrasher() {
        addTrigger(Triggers.attacks(), new ConditionalEffect(
                new PermanentEnteredThisTurn(
                        new CardNotPredicate(new CardTypePredicate(CardType.LAND)), 2),
                new PutCountersOnSourceEffect(1, 1, 1)));
    }
}
