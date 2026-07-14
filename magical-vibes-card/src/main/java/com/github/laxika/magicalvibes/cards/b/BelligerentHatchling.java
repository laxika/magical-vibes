package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;

import java.util.List;

@CardRegistration(set = "EVE", collectorNumber = "134")
public class BelligerentHatchling extends Card {

    public BelligerentHatchling() {
        // This creature enters with four -1/-1 counters on it.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new EnterWithCountersEffect(CounterType.MINUS_ONE_MINUS_ONE, new Fixed(4)));

        // Whenever you cast a red spell, remove a -1/-1 counter from this creature.
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL,
                new SpellCastTriggerEffect(new CardColorPredicate(CardColor.RED),
                        List.of(new RemoveCounterFromSourceEffect(CounterType.MINUS_ONE_MINUS_ONE, 1))));

        // Whenever you cast a white spell, remove a -1/-1 counter from this creature.
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL,
                new SpellCastTriggerEffect(new CardColorPredicate(CardColor.WHITE),
                        List.of(new RemoveCounterFromSourceEffect(CounterType.MINUS_ONE_MINUS_ONE, 1))));
    }
}
