package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnReturnedPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPowerAtMostPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "TDM", collectorNumber = "24")
public class SmileAtDeath extends Card {

    public SmileAtDeath() {
        // Return up to two target creature cards with power 2 or less from your graveyard to the
        // battlefield, then put a +1/+1 counter on each of those creatures.
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new ReturnTargetCardsFromGraveyardToBattlefieldEffect(
                new CardAllOfPredicate(List.of(
                        new CardTypePredicate(CardType.CREATURE),
                        new CardPowerAtMostPredicate(2))),
                2, false, false));
        addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                new PutCounterOnReturnedPermanentsEffect(CounterType.PLUS_ONE_PLUS_ONE, 1));
    }
}
