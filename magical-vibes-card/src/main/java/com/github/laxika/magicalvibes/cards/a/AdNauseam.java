package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AdNauseamEffect;

@CardRegistration(set = "ALA", collectorNumber = "63")
public class AdNauseam extends Card {

    public AdNauseam() {
        // Reveal the top card of your library and put that card into your hand. You lose life
        // equal to its mana value. You may repeat this process any number of times.
        addEffect(EffectSlot.SPELL, new AdNauseamEffect());
    }
}
