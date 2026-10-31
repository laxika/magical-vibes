package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CantBlockEffect;

@CardRegistration(set = "DST", collectorNumber = "51")
public class ScavengingScarab extends Card {

    public ScavengingScarab() {
        addStaticEffect(new CantBlockEffect());
    }
}
