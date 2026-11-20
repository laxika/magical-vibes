package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardOfOwnLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "BFZ", collectorNumber = "61")
public class MistIntruder extends Card {

    public MistIntruder() {
        addTrigger(Triggers.dealsCombatDamageToPlayer(),
                ExileTopCardOfOwnLibraryEffect.of(Subjects.triggeringPlayer()));
    }
}
