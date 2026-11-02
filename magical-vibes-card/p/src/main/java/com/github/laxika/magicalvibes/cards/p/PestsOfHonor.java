package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.condition.PermanentEnteredThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "WOE", collectorNumber = "310")
public class PestsOfHonor extends Card {

    public PestsOfHonor() {
        addTrigger(Triggers.beginningOfCombat(ActivePlayer.YOU), new ConditionalEffect(
                new PermanentEnteredThisTurn(
                        new CardNotPredicate(new CardTypePredicate(CardType.LAND)), 2),
                new PutCountersOnSourceEffect(1, 1, 1)));
    }
}
