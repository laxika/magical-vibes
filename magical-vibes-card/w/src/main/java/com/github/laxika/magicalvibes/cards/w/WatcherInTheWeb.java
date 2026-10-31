package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GrantAdditionalBlockEffect;

@CardRegistration(set = "SOI", collectorNumber = "239")
public class WatcherInTheWeb extends Card {

    public WatcherInTheWeb() {
        addStaticEffect(new GrantAdditionalBlockEffect(7));
    }
}
