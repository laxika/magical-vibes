package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MRD", collectorNumber = "231")
public class PsychogenicProbe extends Card {

    public PsychogenicProbe() {
        addTrigger(Triggers.playerShufflesLibrary(),
                DealDamageEffect.to(Subjects.triggeringPlayer(), 2));
    }
}
