package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;

@CardRegistration(set = "AVR", collectorNumber = "107")
public class HarvesterOfSouls extends Card {

    public HarvesterOfSouls() {
        // Whenever another nontoken creature dies, you may draw a card. Deathtouch comes from Scryfall.
        // The dying permanent has already left the battlefield when the slot is dispatched, so this
        // creature's own death never triggers it ("another").
        addEffect(EffectSlot.ON_ANY_NONTOKEN_CREATURE_DIES,
                new MayEffect(new DrawCardEffect(), "Draw a card?"));
    }
}
