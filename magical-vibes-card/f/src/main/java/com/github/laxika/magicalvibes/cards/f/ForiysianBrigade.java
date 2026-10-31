package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GrantAdditionalBlockEffect;

@CardRegistration(set = "WTH", collectorNumber = "14")
public class ForiysianBrigade extends Card {

    public ForiysianBrigade() {
        addStaticEffect(new GrantAdditionalBlockEffect(1));
    }
}
