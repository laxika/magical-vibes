package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.ControllerLifeAtMost;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DamageLifeFloorEffect;
import com.github.laxika.magicalvibes.model.effect.LifeFloorCondition;
import com.github.laxika.magicalvibes.model.effect.SetLifeTotalEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "M13", collectorNumber = "167")
public class ElderscaleWurm extends Card {

    public ElderscaleWurm() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new ConditionalEffect(new ControllerLifeAtMost(6), new SetLifeTotalEffect(7)));
        addStaticEffect(new DamageLifeFloorEffect(7, LifeFloorCondition.LIFE_AT_LEAST_FLOOR));
    }
}
