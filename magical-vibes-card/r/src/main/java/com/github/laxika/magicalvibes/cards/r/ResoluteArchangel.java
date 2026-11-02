package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.StartingLifeTotal;
import com.github.laxika.magicalvibes.model.condition.ControllerLifeBelowStartingLife;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.SetLifeTotalEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "M15", collectorNumber = "28")
public class ResoluteArchangel extends Card {

    public ResoluteArchangel() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new ConditionalEffect(new ControllerLifeBelowStartingLife(),
                        new SetLifeTotalEffect(new StartingLifeTotal())));
    }
}
