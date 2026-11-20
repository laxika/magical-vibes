package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardOfOwnLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "BFZ", collectorNumber = "91")
public class CullingDrone extends Card {

    public CullingDrone() {
        addTrigger(Triggers.dealsCombatDamageToPlayer(),
                ExileTopCardOfOwnLibraryEffect.of(Subjects.triggeringPlayer()));
    }
}
