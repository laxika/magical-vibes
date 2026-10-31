package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.NonbasicLandsBecomeTypeEffect;

@CardRegistration(set = "MAR", collectorNumber = "55")
public class HarbingerOfTheSeas extends Card {

    public HarbingerOfTheSeas() {
        addStaticEffect(new NonbasicLandsBecomeTypeEffect(CardSubtype.ISLAND));
    }
}
