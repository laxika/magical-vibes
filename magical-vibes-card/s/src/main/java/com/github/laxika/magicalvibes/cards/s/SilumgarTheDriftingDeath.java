package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledByDefendingPlayerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "FRF", collectorNumber = "157")
@CardRegistration(set = "C17", collectorNumber = "194")
public class SilumgarTheDriftingDeath extends Card {

    public SilumgarTheDriftingDeath() {
        // Whenever a Dragon you control attacks, creatures defending player controls get -1/-1
        // until end of turn.
        addTrigger(Triggers.creatureYouControlAttacks()
                        .withFilter(new PermanentHasSubtypePredicate(CardSubtype.DRAGON)),
                new BoostAllCreaturesEffect(-1, -1, new PermanentControlledByDefendingPlayerPredicate()));
    }
}
