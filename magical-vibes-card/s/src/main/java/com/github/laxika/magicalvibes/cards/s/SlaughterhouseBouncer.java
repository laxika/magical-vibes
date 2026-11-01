package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.ControllerHandEmpty;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DIS", collectorNumber = "54")
public class SlaughterhouseBouncer extends Card {

    public SlaughterhouseBouncer() {
        target(TargetFilters.creature()).addTrigger(Triggers.dies(ObjectRelation.THIS),
                new ConditionalEffect(new ControllerHandEmpty(), new BoostTargetCreatureEffect(-3, -3)));
    }
}
