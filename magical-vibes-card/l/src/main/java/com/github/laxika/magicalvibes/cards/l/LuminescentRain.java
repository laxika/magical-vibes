package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GainLifePerChosenTypeCountEffect;

@CardRegistration(set = "MOR", collectorNumber = "129")
public class LuminescentRain extends Card {

    public LuminescentRain() {
        addSpellEffect(new GainLifePerChosenTypeCountEffect(2));
    }
}
