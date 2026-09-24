package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.OpponentDrawTwoOrMoreReplacedEffect;

@CardRegistration(set = "CMM", collectorNumber = "10")
@CardRegistration(set = "CMM", collectorNumber = "455")
public class AlmsCollector extends Card {

    public AlmsCollector() {
        addEffect(EffectSlot.STATIC, new OpponentDrawTwoOrMoreReplacedEffect());
    }
}
