package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.QueueReflexiveAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "IKO", collectorNumber = "205")
public class SavaiThundermane extends Card {

    public SavaiThundermane() {
        addTrigger(Triggers.youCycleCard(),
                new MayPayManaEffect("{2}",
                        new QueueReflexiveAbilityEffect(SequenceEffect.of(
                                new DealDamageToTargetCreatureEffect(2),
                                new GainLifeEffect(2))),
                        "Pay {2} to deal 2 damage to target creature and gain 2 life?"));
    }
}
