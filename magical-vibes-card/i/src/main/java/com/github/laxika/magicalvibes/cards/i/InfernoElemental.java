package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.TriggerMode;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;

@CardRegistration(set = "M10", collectorNumber = "142")
public class InfernoElemental extends Card {

    public InfernoElemental() {
        addEffect(EffectSlot.ON_BLOCK, new DealDamageToTargetCreatureEffect(3));
        addEffect(EffectSlot.ON_BECOMES_BLOCKED, new DealDamageToTargetCreatureEffect(3), TriggerMode.PER_BLOCKER);
    }
}
