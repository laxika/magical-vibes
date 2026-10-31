package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;

@CardRegistration(set = "ALA", collectorNumber = "54")
public class ResoundingWave extends Card {

    public ResoundingWave() {
        // Return target permanent to its owner's hand. (ReturnToHandEffect.target()'s TargetSpec
        // narrows the legal target to a battlefield permanent — no explicit target filter needed.)
        addSpellEffect(ReturnToHandEffect.target());

        // Cycling {5}{W}{U}{B}. "When you cycle this card, return two target permanents to their owners'
        // hands." Both targets are chosen as the trigger is put on the stack; with fewer than two legal
        // targets the trigger is removed and only the cycling draw happens.
        addCycling("{5}{W}{U}{B}");
        addEffect(EffectSlot.ON_SELF_CYCLED, ReturnToHandEffect.target());
        setSelfCycledTriggerPermanentTargets(2);
    }
}
