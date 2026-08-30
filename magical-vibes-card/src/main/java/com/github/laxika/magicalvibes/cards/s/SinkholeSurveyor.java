package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EndureEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "TDM", collectorNumber = "93")
public class SinkholeSurveyor extends Card {

    public SinkholeSurveyor() {
        addEffect(EffectSlot.ON_ATTACK, SequenceEffect.of(
                new LoseLifeEffect(1),
                new EndureEffect(1)));
    }
}
