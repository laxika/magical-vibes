package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "WOE", collectorNumber = "152")
public class TatteredRatter extends Card {

    public TatteredRatter() {
        // Whenever a Rat you control becomes blocked, it gets +2/+0 until end of turn.
        addTrigger(Triggers.creatureYouControlBecomesBlocked()
                        .withFilter(new PermanentHasSubtypePredicate(CardSubtype.RAT)),
                BoostEffect.of(Subjects.triggering(), 2, 0));
    }
}
