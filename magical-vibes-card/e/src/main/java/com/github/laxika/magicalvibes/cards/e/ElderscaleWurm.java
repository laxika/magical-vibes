package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControllerLifeAtMost;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DamageLifeFloorEffect;
import com.github.laxika.magicalvibes.model.effect.LifeFloorCondition;
import com.github.laxika.magicalvibes.model.effect.SetLifeTotalEffect;

@CardRegistration(set = "M13", collectorNumber = "167")
public class ElderscaleWurm extends Card {

    public ElderscaleWurm() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ConditionalEffect(new ControllerLifeAtMost(6), new SetLifeTotalEffect(7)));
        addEffect(EffectSlot.STATIC, new DamageLifeFloorEffect(7, LifeFloorCondition.LIFE_AT_LEAST_FLOOR));
    }
}
