package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedEffect;

@CardRegistration(set = "5DN", collectorNumber = "34")
public class PlasmaElemental extends Card {

    public PlasmaElemental() {
        addStaticEffect(new CantBeBlockedEffect());
    }
}
