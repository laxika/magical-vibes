package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.effect.AssignNoCombatDamageEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "VIS", collectorNumber = "77")
public class DwarvenVigilantes extends Card {

    public DwarvenVigilantes() {
        // Whenever this creature attacks and isn't blocked, you may have it deal damage equal to
        // its power to target creature. If you do, this creature assigns no combat damage this turn.
        addEffect(EffectSlot.ON_ATTACKS_UNBLOCKED,
                new MayEffect(SequenceEffect.of(
                        new DealDamageToTargetCreatureEffect(new SourcePower()),
                        new AssignNoCombatDamageEffect()),
                        "have it deal damage equal to its power to a target creature?"));
    }
}
