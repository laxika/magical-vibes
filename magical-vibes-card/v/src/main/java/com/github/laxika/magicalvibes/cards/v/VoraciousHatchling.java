package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceEffect;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "EVE", collectorNumber = "97")
public class VoraciousHatchling extends Card {

    public VoraciousHatchling() {
        // This creature enters with four -1/-1 counters on it. (Lifelink auto-loaded.)
        addStaticEffect(new EnterWithCountersEffect(CounterType.MINUS_ONE_MINUS_ONE, new Fixed(4)));

        // Whenever you cast a white spell, remove a -1/-1 counter from this creature.
        addTrigger(Triggers.youCastSpell().withFilter(new CardColorPredicate(CardColor.WHITE)),
                new RemoveCounterFromSourceEffect(CounterType.MINUS_ONE_MINUS_ONE, 1));

        // Whenever you cast a black spell, remove a -1/-1 counter from this creature.
        addTrigger(Triggers.youCastSpell().withFilter(new CardColorPredicate(CardColor.BLACK)),
                new RemoveCounterFromSourceEffect(CounterType.MINUS_ONE_MINUS_ONE, 1));
    }
}
