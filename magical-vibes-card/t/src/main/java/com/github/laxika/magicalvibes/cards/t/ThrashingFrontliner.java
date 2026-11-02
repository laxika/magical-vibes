package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.AttackedTargetMatches;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsBattlePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MOM", collectorNumber = "167")
public class ThrashingFrontliner extends Card {

    public ThrashingFrontliner() {
        addTrigger(Triggers.attacks(), new ConditionalEffect(
                new AttackedTargetMatches(new PermanentIsBattlePredicate()),
                new BoostSelfEffect(1, 1)));
    }
}
