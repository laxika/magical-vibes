package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DoubleDrawExceptFirstDrawStepDrawEffect;
import com.github.laxika.magicalvibes.model.effect.DoubleLifeGainEffect;

@CardRegistration(set = "ORI", collectorNumber = "221")
public class AlhammarretsArchive extends Card {

    public AlhammarretsArchive() {
        addEffect(EffectSlot.STATIC, new DoubleLifeGainEffect());
        addEffect(EffectSlot.STATIC, new DoubleDrawExceptFirstDrawStepDrawEffect());
    }
}
