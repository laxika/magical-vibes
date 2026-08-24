package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;

@CardRegistration(set = "APC", collectorNumber = "112")
public class MysticSnake extends Card {

    public MysticSnake() {
        // Flash is auto-loaded from Scryfall.

        // When this creature enters, counter target spell.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new CounterSpellEffect());
    }
}
