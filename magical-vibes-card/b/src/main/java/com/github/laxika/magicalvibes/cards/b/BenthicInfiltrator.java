package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardOfOwnLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "BFZ", collectorNumber = "55")
public class BenthicInfiltrator extends Card {

    public BenthicInfiltrator() {
        addStaticEffect(new CantBeBlockedEffect());
        addTrigger(Triggers.dealsCombatDamageToPlayer(),
                ExileTopCardOfOwnLibraryEffect.of(Subjects.triggeringPlayer()));
    }
}
