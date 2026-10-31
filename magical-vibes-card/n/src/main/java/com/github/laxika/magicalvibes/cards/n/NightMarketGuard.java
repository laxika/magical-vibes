package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GrantAdditionalBlockEffect;

@CardRegistration(set = "AER", collectorNumber = "166")
public class NightMarketGuard extends Card {

    public NightMarketGuard() {
        addStaticEffect(new GrantAdditionalBlockEffect(1));
    }
}
