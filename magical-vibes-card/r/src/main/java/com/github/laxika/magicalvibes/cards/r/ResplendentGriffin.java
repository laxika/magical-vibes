package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControllerHasCityBlessing;
import com.github.laxika.magicalvibes.model.effect.AscendEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;

@CardRegistration(set = "RIX", collectorNumber = "170")
public class ResplendentGriffin extends Card {

    public ResplendentGriffin() {
        addEffect(EffectSlot.STATIC, new AscendEffect());
        addEffect(EffectSlot.ON_ATTACK, new ConditionalEffect(
                new ControllerHasCityBlessing(), new PutCountersOnSourceEffect(1, 1, 1)));
    }
}
