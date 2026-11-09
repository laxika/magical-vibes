package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.Set;

@CardRegistration(set = "MMQ", collectorNumber = "46")
public class RighteousIndignation extends Card {

    public RighteousIndignation() {
        // "Whenever a creature blocks a black or red creature, the blocking creature gets +1/+1": for each blocker
        // of a black or red attacker, the blocker the entry's "that creature".
        addTrigger(Triggers.creatureBecomesBlocked()
                        .withFilter(new PermanentColorInPredicate(Set.of(CardColor.BLACK, CardColor.RED))),
                TriggerOptions.EACH_BLOCKER,
                new BoostTargetCreatureEffect(1, 1));
    }
}
