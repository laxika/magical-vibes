package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.AllOf;
import com.github.laxika.magicalvibes.model.condition.GraveyardCardThreshold;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "ECL", collectorNumber = "96")
public class CreakwoodSafewright extends Card {

    public CreakwoodSafewright() {
        addStaticEffect(new EnterWithCountersEffect(CounterType.MINUS_ONE_MINUS_ONE, new Fixed(3)));

        addTrigger(Triggers.endStep(ActivePlayer.YOU),
                TriggerOptions.DEFAULT.withInterveningIf(
                        new AllOf(List.of(
                                new GraveyardCardThreshold(1, new CardSubtypePredicate(CardSubtype.ELF)),
                                new SourceCounterThreshold(1, CounterType.MINUS_ONE_MINUS_ONE)))),
                new RemoveCounterFromSourceEffect(CounterType.MINUS_ONE_MINUS_ONE, 1));
    }
}
