package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ProliferateEffect;

@CardRegistration(set = "SLD", collectorNumber = "453")
public class AtraxaPraetorsVoice extends Card {

    public AtraxaPraetorsVoice() {
        // At the beginning of your end step, proliferate.
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED, new ProliferateEffect());
    }
}
