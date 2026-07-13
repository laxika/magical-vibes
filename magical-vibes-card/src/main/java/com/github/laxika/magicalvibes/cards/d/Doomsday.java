package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DoomsdayEffect;

@CardRegistration(set = "6ED", collectorNumber = "121")
public class Doomsday extends Card {

    public Doomsday() {
        addEffect(EffectSlot.SPELL, new DoomsdayEffect());
    }
}
