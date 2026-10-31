package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CantHaveCountersEffect;

@CardRegistration(set = "SHM", collectorNumber = "264")
public class Tatterkite extends Card {

    public Tatterkite() {
        // Flying is auto-loaded from Scryfall keywords.
        addStaticEffect(new CantHaveCountersEffect());
    }
}
