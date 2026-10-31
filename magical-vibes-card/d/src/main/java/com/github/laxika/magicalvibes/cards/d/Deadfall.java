package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.LandwalkIgnoredForBlockingEffect;

@CardRegistration(set = "LEG", collectorNumber = "181")
public class Deadfall extends Card {

    public Deadfall() {
        addStaticEffect(new LandwalkIgnoredForBlockingEffect(Keyword.FORESTWALK));
    }
}
