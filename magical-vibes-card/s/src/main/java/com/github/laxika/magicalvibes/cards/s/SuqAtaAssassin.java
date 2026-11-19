package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GivePoisonCountersEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "VIS", collectorNumber = "69")
public class SuqAtaAssassin extends Card {

    public SuqAtaAssassin() {
        // Fear is auto-loaded from Scryfall.
        // Whenever this creature attacks and isn't blocked, defending player gets a poison counter.
        addTrigger(Triggers.attacksUnblocked(), new GivePoisonCountersEffect(1, Subjects.targetPlayer()));
    }
}
