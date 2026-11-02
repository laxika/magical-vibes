package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.MinimumAttackers;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MSC", collectorNumber = "595")
public class GeneralThunderboltRoss extends Card {

    public GeneralThunderboltRoss() {
        addTrigger(Triggers.attacks(), new ConditionalEffect(
                new MinimumAttackers(3),
                new BoostAllCreaturesEffect(1, 0, new PermanentIsAttackingPredicate())));
    }
}
