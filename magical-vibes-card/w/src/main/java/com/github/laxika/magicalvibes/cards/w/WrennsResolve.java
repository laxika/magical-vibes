package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsMayPlayUntilNextTurnEffect;

@CardRegistration(set = "MOM", collectorNumber = "173")
public class WrennsResolve extends Card {

    public WrennsResolve() {
        addSpellEffect(new ExileTopCardsMayPlayUntilNextTurnEffect(2));
    }
}
