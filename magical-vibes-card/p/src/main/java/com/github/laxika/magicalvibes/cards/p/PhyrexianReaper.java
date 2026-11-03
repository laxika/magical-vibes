package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.Set;

@CardRegistration(set = "INV", collectorNumber = "117")
public class PhyrexianReaper extends Card {

    public PhyrexianReaper() {
        addTrigger(Triggers.becomesBlocked().withFilter(new PermanentColorInPredicate(Set.of(CardColor.GREEN))),
                TriggerOptions.EACH_BLOCKER,
                new DestroyTargetPermanentEffect(true));
    }
}
