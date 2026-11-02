package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.condition.AllOf;
import com.github.laxika.magicalvibes.model.condition.ControllerCastSpellThisTurn;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.filter.CardTruePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "OTJ", collectorNumber = "17")
public class InventiveWingsmith extends Card {

    public InventiveWingsmith() {
        // At the beginning of your end step, if you haven't cast a spell from your hand this turn
        // and this creature doesn't have a flying counter on it, put a flying counter on it.
        addTrigger(Triggers.endStep(ActivePlayer.YOU), new ConditionalEffect(
                new AllOf(List.of(
                        new NotCondition(new ControllerCastSpellThisTurn(new CardTruePredicate(), true)),
                        new NotCondition(new SourceCounterThreshold(1, CounterType.FLYING)))),
                new PutCountersOnSelfEffect(CounterType.FLYING)));
    }
}
