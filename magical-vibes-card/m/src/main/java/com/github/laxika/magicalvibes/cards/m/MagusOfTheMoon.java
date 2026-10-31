package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.NonbasicLandsBecomeTypeEffect;

@CardRegistration(set = "FUT", collectorNumber = "101")
@CardRegistration(set = "IMA", collectorNumber = "138")
@CardRegistration(set = "SLD", collectorNumber = "1761")
@CardRegistration(set = "TSR", collectorNumber = "175")
@CardRegistration(set = "SPG", collectorNumber = "125")
public class MagusOfTheMoon extends Card {

    public MagusOfTheMoon() {
        addStaticEffect(new NonbasicLandsBecomeTypeEffect(CardSubtype.MOUNTAIN));
    }
}
