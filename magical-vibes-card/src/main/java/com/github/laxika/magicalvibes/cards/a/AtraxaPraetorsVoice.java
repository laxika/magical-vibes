package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ProliferateEffect;

@CardRegistration(set = "2XM", collectorNumber = "190")
public class AtraxaPraetorsVoice extends Card {

    public AtraxaPraetorsVoice() {
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED, new ProliferateEffect());
    }
}
