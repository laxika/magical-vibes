package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;

@CardRegistration(set = "M20", collectorNumber = "214")
public class MoldervineReclamation extends Card {

    public MoldervineReclamation() {
        // Whenever a creature you control dies, you gain 1 life and draw a card.
        addEffect(EffectSlot.ON_ALLY_CREATURE_DIES, new GainLifeEffect(1));
        addEffect(EffectSlot.ON_ALLY_CREATURE_DIES, new DrawCardEffect(1));
    }
}
