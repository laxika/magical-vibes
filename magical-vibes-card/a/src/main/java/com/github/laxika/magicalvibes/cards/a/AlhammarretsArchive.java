package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DoubleDrawExceptFirstDrawStepDrawEffect;
import com.github.laxika.magicalvibes.model.effect.DoubleLifeGainEffect;

@CardRegistration(set = "ORI", collectorNumber = "221")
@CardRegistration(set = "C21", collectorNumber = "233")
@CardRegistration(set = "SLC", collectorNumber = "27")
public class AlhammarretsArchive extends Card {

    public AlhammarretsArchive() {
        addStaticEffect(new DoubleLifeGainEffect());
        addStaticEffect(new DoubleDrawExceptFirstDrawStepDrawEffect());
    }
}
