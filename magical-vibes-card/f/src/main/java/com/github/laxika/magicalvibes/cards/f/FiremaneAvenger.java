package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.MinimumAttackers;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "GTC", collectorNumber = "163")
@CardRegistration(set = "GK1", collectorNumber = "89")
public class FiremaneAvenger extends Card {

    public FiremaneAvenger() {
        // Battalion — Whenever this creature and at least two other creatures attack,
        // this creature deals 3 damage to any target and you gain 3 life.
        addTrigger(Triggers.attacks(),
                TriggerOptions.DEFAULT.withEventCondition(new MinimumAttackers(3)),
                SequenceEffect.of(new DealDamageToAnyTargetEffect(3, false), new GainLifeEffect(3)));
    }
}
