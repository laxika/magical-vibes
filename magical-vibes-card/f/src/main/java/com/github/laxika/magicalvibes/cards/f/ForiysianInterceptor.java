package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GrantAdditionalBlockEffect;

@CardRegistration(set = "TSP", collectorNumber = "18")
public class ForiysianInterceptor extends Card {

    public ForiysianInterceptor() {
        addStaticEffect(new GrantAdditionalBlockEffect(1));
    }
}
