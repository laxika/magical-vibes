package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TLE", collectorNumber = "107")
public class GiantFly extends Card {

    public GiantFly() {
        addTrigger(Triggers.sacrificed(
                        new PermanentNotPredicate(new PermanentIsSourceCardPredicate()),
                        ObjectRelation.ANOTHER_YOU_CONTROL),
                new BoostSelfEffect(1, 0));
    }
}
