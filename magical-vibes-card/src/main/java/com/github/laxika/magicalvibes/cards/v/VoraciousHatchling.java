package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromEachControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;

import java.util.List;

@CardRegistration(set = "EVE", collectorNumber = "97")
public class VoraciousHatchling extends Card {

    public VoraciousHatchling() {
        // This creature enters with four -1/-1 counters on it. (Lifelink auto-loaded.)
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new EnterWithCountersEffect(CounterType.MINUS_ONE_MINUS_ONE, new Fixed(4)));

        // Whenever you cast a white spell, remove a -1/-1 counter from this creature.
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL,
                new SpellCastTriggerEffect(new CardColorPredicate(CardColor.WHITE),
                        List.of(new RemoveCounterFromEachControlledPermanentEffect(
                                CounterType.MINUS_ONE_MINUS_ONE, 1, new PermanentIsSourceCardPredicate()))));

        // Whenever you cast a black spell, remove a -1/-1 counter from this creature.
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL,
                new SpellCastTriggerEffect(new CardColorPredicate(CardColor.BLACK),
                        List.of(new RemoveCounterFromEachControlledPermanentEffect(
                                CounterType.MINUS_ONE_MINUS_ONE, 1, new PermanentIsSourceCardPredicate()))));
    }
}
