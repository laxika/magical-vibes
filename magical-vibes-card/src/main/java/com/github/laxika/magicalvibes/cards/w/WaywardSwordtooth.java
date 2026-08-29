package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControllerHasCityBlessing;
import com.github.laxika.magicalvibes.model.effect.AscendEffect;
import com.github.laxika.magicalvibes.model.effect.CantAttackOrBlockUnlessEffect;
import com.github.laxika.magicalvibes.model.effect.PlaysAdditionalLandEachTurnEffect;

@CardRegistration(set = "RIX", collectorNumber = "150")
public class WaywardSwordtooth extends Card {

    public WaywardSwordtooth() {
        addEffect(EffectSlot.STATIC, new AscendEffect());
        addEffect(EffectSlot.STATIC, new PlaysAdditionalLandEachTurnEffect(1));
        addEffect(EffectSlot.STATIC, new CantAttackOrBlockUnlessEffect(
                new ControllerHasCityBlessing(),
                "you have the city's blessing"));
    }
}
