package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureDamagedPlayerControlsEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ODY", collectorNumber = "222")
public class SparkMage extends Card {

    public SparkMage() {
        addTrigger(Triggers.dealsCombatDamageToPlayer(),
                new MayEffect(new DealDamageToTargetCreatureDamagedPlayerControlsEffect(1),
                        "You may have Spark Mage deal 1 damage to target creature that player controls."));
    }
}
