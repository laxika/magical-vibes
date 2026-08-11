package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;

import java.util.List;

@CardRegistration(set = "ECL", collectorNumber = "14")
public class EncumberedReejerey extends Card {

    public EncumberedReejerey() {
        // This creature enters with three -1/-1 counters on it.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new EnterWithCountersEffect(CounterType.MINUS_ONE_MINUS_ONE, new Fixed(3)));

        // Whenever this creature becomes tapped while it has a -1/-1 counter on it, remove a
        // -1/-1 counter from it. The triggering permanent filter checks the counter when the
        // ability triggers; the conditional wrapper checks it again when the ability resolves.
        addEffect(EffectSlot.ON_ALLY_PERMANENT_BECOMES_TAPPED,
                new TriggeringPermanentConditionalEffect(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsSourceCardPredicate(),
                                new PermanentHasCountersPredicate(CounterType.MINUS_ONE_MINUS_ONE))),
                        new ConditionalEffect(
                                new SourceCounterThreshold(1, CounterType.MINUS_ONE_MINUS_ONE),
                                new RemoveCounterFromSourceEffect(CounterType.MINUS_ONE_MINUS_ONE, 1))));
    }
}
