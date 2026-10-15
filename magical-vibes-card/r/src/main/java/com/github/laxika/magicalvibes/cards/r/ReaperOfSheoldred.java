package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DamageSourceControllerGetsPoisonCounterEffect;

@CardRegistration(set = "NPH", collectorNumber = "72")
public class ReaperOfSheoldred extends Card {

    public ReaperOfSheoldred() {
        // Whenever a source deals damage to this creature, that source's controller gets a poison counter.
        addEffect(EffectSlot.ON_DEALT_DAMAGE, new DamageSourceControllerGetsPoisonCounterEffect(null));
    }
}
