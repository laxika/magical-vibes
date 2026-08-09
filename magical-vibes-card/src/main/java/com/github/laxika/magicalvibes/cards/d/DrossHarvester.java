package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromColorsEffect;

import java.util.Set;

@CardRegistration(set = "MRD", collectorNumber = "63")
public class DrossHarvester extends Card {

    public DrossHarvester() {
        addEffect(EffectSlot.STATIC, new ProtectionFromColorsEffect(Set.of(CardColor.WHITE)));
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED, new LoseLifeEffect(4));
        addEffect(EffectSlot.ON_ANY_CREATURE_DIES, new GainLifeEffect(2));
        addEffect(EffectSlot.ON_DEATH, new GainLifeEffect(2));
    }
}
