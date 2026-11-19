package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "P02", collectorNumber = "61")
public class AbyssalNightstalker extends Card {

    public AbyssalNightstalker() {
        // Whenever this creature attacks and isn't blocked, defending player discards a card.
        addTrigger(Triggers.attacksUnblocked(), new DiscardEffect(1, Subjects.targetPlayer(), false));
    }
}
