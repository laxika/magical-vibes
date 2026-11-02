package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MMQ", collectorNumber = "52")
public class TaskForce extends Card {

    public TaskForce() {
        addTrigger(Triggers.becomesTargetOfSpellOrAbility(), new BoostSelfEffect(0, 3));
    }
}
