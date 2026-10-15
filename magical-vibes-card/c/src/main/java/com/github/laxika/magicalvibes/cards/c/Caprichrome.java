package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsAsEntersForCountersEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

@CardRegistration(set = "MH2", collectorNumber = "9")
public class Caprichrome extends Card {

    public Caprichrome() {
        // Devour artifact 1 (As this creature enters, you may sacrifice any number of artifacts.
        // It enters with that many +1/+1 counters on it.)
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new SacrificePermanentsAsEntersForCountersEffect(new PermanentIsArtifactPredicate(), 1));
    }
}
