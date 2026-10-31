package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "ALA", collectorNumber = "174")
public class JhessianInfiltrator extends Card {

    public JhessianInfiltrator() {
        addStaticEffect(new CantBeBlockedEffect());
    }
}
