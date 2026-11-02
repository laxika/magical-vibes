package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.ExtraTurn;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.CantAttackUnlessEffect;
import com.github.laxika.magicalvibes.model.effect.ControllerExtraTurnEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "THS", collectorNumber = "196")
@CardRegistration(set = "PIO", collectorNumber = "233")
public class MedomaiTheAgeless extends Card {

    public MedomaiTheAgeless() {
        addTrigger(Triggers.dealsCombatDamageToPlayer(), new ControllerExtraTurnEffect(1));
        addStaticEffect(new CantAttackUnlessEffect(
                new NotCondition(new ExtraTurn()),
                "it isn't an extra turn"
        ));
    }
}
