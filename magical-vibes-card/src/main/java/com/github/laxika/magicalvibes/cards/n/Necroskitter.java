package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReturnDyingOpponentCreatureUnderYourControlEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate;

@CardRegistration(set = "EVE", collectorNumber = "38")
public class Necroskitter extends Card {

    public Necroskitter() {
        // Wither is auto-loaded from Scryfall. Whenever a creature an opponent controls with a
        // -1/-1 counter on it dies, you may return that card to the battlefield under your control.
        // The intervening-if is evaluated against the dying permanent's counters.
        addEffect(EffectSlot.ON_OPPONENT_CREATURE_DIES, new TriggeringPermanentConditionalEffect(
                new PermanentHasCountersPredicate(CounterType.MINUS_ONE_MINUS_ONE),
                new ReturnDyingOpponentCreatureUnderYourControlEffect()));
    }
}
