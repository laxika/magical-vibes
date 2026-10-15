package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.MiracleCast;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.DistributeCountersAmongTargetsEffect;

@CardRegistration(set = "AVR", collectorNumber = "168")
public class BlessingsOfNature extends Card {

    public BlessingsOfNature() {
        // Miracle {G}
        addCastingOption(new MiracleCast("{G}"));

        // Distribute four +1/+1 counters among any number of target creatures.
        // The division is announced as the spell is cast and rides on the stack entry's
        // assignment map (no target() call).
        addEffect(EffectSlot.SPELL, DistributeCountersAmongTargetsEffect.chosenAmongTargetCreatures(
                CounterType.PLUS_ONE_PLUS_ONE, new Fixed(4)));
    }
}
