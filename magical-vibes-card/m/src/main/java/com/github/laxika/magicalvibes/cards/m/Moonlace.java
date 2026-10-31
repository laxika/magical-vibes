package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SetTargetColorEffect;

@CardRegistration(set = "TSP", collectorNumber = "68")
public class Moonlace extends Card {

    public Moonlace() {
        addSpellEffect(new SetTargetColorEffect(null));
    }
}
