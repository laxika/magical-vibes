package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutSlimeCounterAndCreateOozeTokenEffect;

@CardRegistration(set = "ISD", collectorNumber = "186")
public class GutterGrime extends Card {

    public GutterGrime() {
        // Whenever a nontoken creature you control dies, put a slime counter on Gutter Grime,
        // then create a green Ooze creature token with "This creature's power and toughness are
        // each equal to the number of slime counters on Gutter Grime."
        addEffect(EffectSlot.ON_ALLY_NONTOKEN_CREATURE_DIES, new PutSlimeCounterAndCreateOozeTokenEffect());
    }
}
