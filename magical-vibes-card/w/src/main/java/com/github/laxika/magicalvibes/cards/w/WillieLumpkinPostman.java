package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.AttackedTargetIsOpponent;
import com.github.laxika.magicalvibes.model.effect.DamagedPlayerMayDrawAndCantAttackControllerUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedEffect;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MSC", collectorNumber = "101")
@CardRegistration(set = "MSC", collectorNumber = "425")
public class WillieLumpkinPostman extends Card {

    public WillieLumpkinPostman() {
        addStaticEffect(new CantBeBlockedEffect());
        addTrigger(Triggers.dealsCombatDamageToPlayer(),
                TriggerOptions.DEFAULT.withEventCondition(new AttackedTargetIsOpponent()), SequenceEffect.of(
                new DrawCardEffect(),
                new DamagedPlayerMayDrawAndCantAttackControllerUntilNextTurnEffect()));
    }
}
