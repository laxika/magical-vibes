package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RollWithAdvantageEffect;

@CardRegistration(set = "AFR", collectorNumber = "66")
public class PixieGuide extends Card {

    public PixieGuide() {
        addStaticEffect(new RollWithAdvantageEffect());
    }
}
