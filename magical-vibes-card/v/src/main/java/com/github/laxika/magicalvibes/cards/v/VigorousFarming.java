package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.ControlledLandsEnterUntappedEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyGiveLandExtraManaEffect;

@CardRegistration(set = "YBLB", collectorNumber = "19")
public class VigorousFarming extends Card {

    public VigorousFarming() {
        addEffect(EffectSlot.STATIC, new ControlledLandsEnterUntappedEffect());
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED,
                new PerpetuallyGiveLandExtraManaEffect(ManaColor.GREEN));
    }
}
