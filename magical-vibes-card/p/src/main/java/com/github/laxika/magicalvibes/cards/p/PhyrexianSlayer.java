package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.effect.DestroyCreatureBlockingThisEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.Set;

@CardRegistration(set = "INV", collectorNumber = "118")
public class PhyrexianSlayer extends Card {

    public PhyrexianSlayer() {
        // Whenever Phyrexian Slayer becomes blocked by a white creature, destroy that creature.
        // It can't be regenerated. The trigger is created once per qualifying blocker.
        addTrigger(Triggers.becomesBlocked().withFilter(new PermanentColorInPredicate(Set.of(CardColor.WHITE))),
                TriggerOptions.EACH_BLOCKER,
                new DestroyCreatureBlockingThisEffect(true));
    }
}
