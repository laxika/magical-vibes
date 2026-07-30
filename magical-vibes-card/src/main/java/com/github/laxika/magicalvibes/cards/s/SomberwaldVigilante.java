package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.TriggerMode;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;

@CardRegistration(set = "AVR", collectorNumber = "156")
public class SomberwaldVigilante extends Card {

    public SomberwaldVigilante() {
        addEffect(EffectSlot.ON_BECOMES_BLOCKED, new DealDamageToTargetCreatureEffect(1), TriggerMode.PER_BLOCKER);
    }
}
