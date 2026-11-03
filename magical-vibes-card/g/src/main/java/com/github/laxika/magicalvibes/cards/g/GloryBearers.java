package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "THB", collectorNumber = "17")
public class GloryBearers extends Card {

    public GloryBearers() {
        // Whenever another creature you control attacks, it gets +0/+1 until end of turn.
        addTrigger(Triggers.creatureYouControlAttacks()
                        .withFilter(new PermanentNotPredicate(new PermanentIsSourcePermanentPredicate())),
                new BoostTargetCreatureEffect(0, 1));
    }
}
