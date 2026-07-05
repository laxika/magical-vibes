package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.SurveilEffect;

@CardRegistration(set = "SOS", collectorNumber = "136")
public class UnsubtleMockery extends Card {

    public UnsubtleMockery() {
        // Unsubtle Mockery deals 4 damage to target creature.
        addEffect(EffectSlot.SPELL, new DealDamageToTargetCreatureEffect(4));

        // Surveil 1.
        addEffect(EffectSlot.SPELL, new SurveilEffect(1));
    }
}
