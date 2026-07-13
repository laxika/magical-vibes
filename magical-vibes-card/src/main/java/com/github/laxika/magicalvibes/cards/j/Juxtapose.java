package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.JuxtaposeEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "6ED", collectorNumber = "77")
public class Juxtapose extends Card {

    public Juxtapose() {
        addEffect(EffectSlot.SPELL, new JuxtaposeEffect());
    }
}
