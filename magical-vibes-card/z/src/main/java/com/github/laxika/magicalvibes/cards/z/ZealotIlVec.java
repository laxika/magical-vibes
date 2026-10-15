package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AssignNoCombatDamageEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "TSP", collectorNumber = "47")
public class ZealotIlVec extends Card {

    public ZealotIlVec() {
        // Whenever this creature attacks and isn't blocked, you may have it deal 1 damage to
        // target creature. If you do, prevent all combat damage this creature would deal this turn.
        addEffect(EffectSlot.ON_ATTACKS_UNBLOCKED,
                new MayEffect(SequenceEffect.of(
                        new DealDamageToTargetCreatureEffect(1),
                        new AssignNoCombatDamageEffect()),
                        "have it deal 1 damage to a target creature?"));
    }
}
