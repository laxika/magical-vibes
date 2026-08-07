package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DistributeCountersAmongCreaturesOnDeathEffect;

@CardRegistration(set = "CHK", collectorNumber = "217")
public class JuganTheRisingStar extends Card {

    public JuganTheRisingStar() {
        // When Jugan dies, you may distribute five +1/+1 counters among any number of target creatures.
        // The division is chosen at resolution via pendingETBDamageAssignments; any creature on the
        // battlefield may receive counters, not just ones you control.
        addEffect(EffectSlot.ON_DEATH,
                DistributeCountersAmongCreaturesOnDeathEffect.fixedAmongAnyCreatures(
                        CounterType.PLUS_ONE_PLUS_ONE, 5));
    }
}
