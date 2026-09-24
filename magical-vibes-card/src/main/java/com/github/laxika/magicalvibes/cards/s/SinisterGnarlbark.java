package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BlightEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "ECC", collectorNumber = "12")
@CardRegistration(set = "ECC", collectorNumber = "32")
public class SinisterGnarlbark extends Card {

    public SinisterGnarlbark() {
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED,
                SequenceEffect.of(new DrawCardEffect(1), new BlightEffect(1, null)));
    }
}
