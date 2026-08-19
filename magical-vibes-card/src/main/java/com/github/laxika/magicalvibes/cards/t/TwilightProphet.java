package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControllerHasCityBlessing;
import com.github.laxika.magicalvibes.model.effect.AscendEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardPutIntoHandAndDrainOpponentsEffect;

@CardRegistration(set = "RIX", collectorNumber = "88")
public class TwilightProphet extends Card {

    public TwilightProphet() {
        addEffect(EffectSlot.STATIC, new AscendEffect());
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new ConditionalEffect(new ControllerHasCityBlessing(),
                new RevealTopCardPutIntoHandAndDrainOpponentsEffect()));
    }
}
