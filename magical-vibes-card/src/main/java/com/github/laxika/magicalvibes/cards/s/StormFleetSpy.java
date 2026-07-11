package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.condition.Raid;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;

@CardRegistration(set = "XLN", collectorNumber = "84")
public class StormFleetSpy extends Card {

    public StormFleetSpy() {
        // Raid — When this creature enters, if you attacked this turn, draw a card.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ConditionalEffect(new Raid(), new DrawCardEffect()));
    }
}
