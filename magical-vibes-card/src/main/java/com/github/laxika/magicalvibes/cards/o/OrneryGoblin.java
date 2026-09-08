package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.TriggerMode;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;

@CardRegistration(set = "GRN", collectorNumber = "112")
public class OrneryGoblin extends Card {

    public OrneryGoblin() {
        addEffect(EffectSlot.ON_BLOCK, new DealDamageToTargetCreatureEffect(1));
        addEffect(EffectSlot.ON_BECOMES_BLOCKED, new DealDamageToTargetCreatureEffect(1), TriggerMode.PER_BLOCKER);
    }
}
