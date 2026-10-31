package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GrantAdditionalBlockEffect;

@CardRegistration(set = "XLN", collectorNumber = "209")
public class SpikeTailedCeratops extends Card {

    public SpikeTailedCeratops() {
        addStaticEffect(new GrantAdditionalBlockEffect(1));
    }
}
