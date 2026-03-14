package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.TriggerMode;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;

@CardRegistration(set = "ISD", collectorNumber = "128")
public class AshmouthHound extends Card {

    public AshmouthHound() {
        addEffect(EffectSlot.ON_BLOCK, new DealDamageToTargetCreatureEffect(1));
        addEffect(EffectSlot.ON_BECOMES_BLOCKED, new DealDamageToTargetCreatureEffect(1), TriggerMode.PER_BLOCKER);
    }
}
