package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;


@CardRegistration(set = "ECL", collectorNumber = "14")
public class EncumberedReejerey extends Card {

    public EncumberedReejerey() {
        // This creature enters with three -1/-1 counters on it.
        addStaticEffect(new EnterWithCountersEffect(CounterType.MINUS_ONE_MINUS_ONE, new Fixed(3)));

        // Whenever this creature becomes tapped while it has a -1/-1 counter on it, remove a
        // -1/-1 counter from it. The trigger's filter checks the counter when the ability
        // triggers; the intervening "if" checks it again when the ability resolves.
        addTrigger(Triggers.becomesTapped()
                        .withFilter(new PermanentHasCountersPredicate(CounterType.MINUS_ONE_MINUS_ONE)),
                TriggerOptions.DEFAULT.withInterveningIf(
                        new SourceCounterThreshold(1, CounterType.MINUS_ONE_MINUS_ONE)),
                new RemoveCounterFromSourceEffect(CounterType.MINUS_ONE_MINUS_ONE, 1));
    }
}
