package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.QueueReflexiveAbilityEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "M19", collectorNumber = "159")
@CardRegistration(set = "GN2", collectorNumber = "43")
public class SparktongueDragon extends Card {

    public SparktongueDragon() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), UnlessPaysEffect.youMayPay("{2}{R}", new QueueReflexiveAbilityEffect(DealDamageEffect.toAnyTarget(3)))
                .withPrompt("Pay {2}{R} to deal 3 damage to any target?"));
    }
}
