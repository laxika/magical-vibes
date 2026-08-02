package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileTargetSpellWithDelayCountersEffect;

@CardRegistration(set = "TMP", collectorNumber = "61")
public class ErtaisMeddling extends Card {

    public ErtaisMeddling() {
        // The upkeep trigger that removes the delay counters and puts the card back onto the stack
        // is handled by StepTriggerService scanning GameData.delayedSpellExiles.
        addEffect(EffectSlot.SPELL, new ExileTargetSpellWithDelayCountersEffect());
    }
}
