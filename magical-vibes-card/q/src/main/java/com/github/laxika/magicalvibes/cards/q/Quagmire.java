package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.LandwalkIgnoredForBlockingEffect;

@CardRegistration(set = "LEG", collectorNumber = "115")
public class Quagmire extends Card {

    public Quagmire() {
        addStaticEffect(new LandwalkIgnoredForBlockingEffect(Keyword.SWAMPWALK));
    }
}
