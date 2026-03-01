package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyEquipmentAttachedToTargetCreatureEffect;

@CardRegistration(set = "SOM", collectorNumber = "106")
public class TurnToSlag extends Card {

    public TurnToSlag() {
        addEffect(EffectSlot.SPELL, new DestroyEquipmentAttachedToTargetCreatureEffect());
        addEffect(EffectSlot.SPELL, new DealDamageToTargetCreatureEffect(5));
    }
}
