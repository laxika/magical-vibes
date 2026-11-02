package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCount;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "LCI", collectorNumber = "136")
public class BrazenBlademaster extends Card {

    public BrazenBlademaster() {
        addTrigger(Triggers.attacks(),
                ConditionalEffect.atTriggerTime(
                        new ControlsPermanentCount(2, new PermanentIsArtifactPredicate()),
                        new BoostSelfEffect(2, 1)));
    }
}
