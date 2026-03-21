package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageToTargetFromChosenSourceEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "DOM", collectorNumber = "20")
public class HealingGrace extends Card {

    public HealingGrace() {
        addEffect(EffectSlot.SPELL, new PreventDamageToTargetFromChosenSourceEffect(3));
        addEffect(EffectSlot.SPELL, new GainLifeEffect(3));
    }
}
