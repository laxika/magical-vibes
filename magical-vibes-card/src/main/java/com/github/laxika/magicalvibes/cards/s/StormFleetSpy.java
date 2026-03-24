package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.RaidConditionalEffect;

@CardRegistration(set = "XLN", collectorNumber = "84")
public class StormFleetSpy extends Card {

    public StormFleetSpy() {
        // Raid — When this creature enters, if you attacked this turn, draw a card.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new RaidConditionalEffect(new DrawCardEffect()));
    }
}
