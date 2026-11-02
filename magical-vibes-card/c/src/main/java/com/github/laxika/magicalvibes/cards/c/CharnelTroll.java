package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.ExileCardFromGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.ForcedCostOrElseEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "GRN", collectorNumber = "160")
public class CharnelTroll extends Card {

    public CharnelTroll() {
        // At the beginning of your upkeep, exile a creature card from your graveyard. If you do,
        // put a +1/+1 counter on this creature. Otherwise, sacrifice it.
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new ForcedCostOrElseEffect(
                new ExileCardFromGraveyardCost(CardType.CREATURE),
                List.of(new SacrificeSelfEffect()), false,
                List.of(new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE))));

        // {B}{G}, Discard a creature card: Put a +1/+1 counter on this creature.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{B}{G}",
                List.of(
                        new DiscardCardTypeCost(new CardTypePredicate(CardType.CREATURE), "creature"),
                        new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE)
                ),
                "{B}{G}, Discard a creature card: Put a +1/+1 counter on Charnel Troll."
        ));
    }
}
