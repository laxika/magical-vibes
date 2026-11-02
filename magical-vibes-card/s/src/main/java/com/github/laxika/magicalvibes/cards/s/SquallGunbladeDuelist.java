package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.condition.PlayerAttacksOneOfYourOpponentsWithPowerOrToughnessEqualToSourceChosenNumber;
import com.github.laxika.magicalvibes.model.effect.ChooseNumberOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAttackedTargetEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "FIC", collectorNumber = "453")
public class SquallGunbladeDuelist extends Card {

    public SquallGunbladeDuelist() {
        addStaticEffect(new ChooseNumberOnEnterEffect(0, 20));
        addTrigger(Triggers.playerAttacks(),
                new ConditionalEffect(
                        new PlayerAttacksOneOfYourOpponentsWithPowerOrToughnessEqualToSourceChosenNumber(),
                        new DealDamageToAttackedTargetEffect(new SourcePower())));
    }
}
