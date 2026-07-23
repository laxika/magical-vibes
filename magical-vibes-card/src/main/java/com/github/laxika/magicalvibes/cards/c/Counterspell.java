package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "4ED", collectorNumber = "65")
@CardRegistration(set = "5ED", collectorNumber = "77")
@CardRegistration(set = "6ED", collectorNumber = "61")
@CardRegistration(set = "7ED", collectorNumber = "67")
@CardRegistration(set = "ICE", collectorNumber = "64")
public class Counterspell extends Card {

    public Counterspell() {
        addEffect(EffectSlot.SPELL, new CounterSpellEffect());
    }
}
