package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MB1", collectorNumber = "48")
public class SwarmOfLocus extends Card {

    public SwarmOfLocus() {
        // Whenever Swarm of Locus attacks, it gets +1/+0 until end of turn for each Locus you control.
        addTrigger(Triggers.attacks(), new BoostSelfEffect(
                new PermanentCount(
                        new PermanentHasSubtypePredicate(CardSubtype.LOCUS),
                        CountScope.CONTROLLER),
                new Fixed(0)));
    }
}
