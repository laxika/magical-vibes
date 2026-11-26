package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.effect.DamageFromSourceCost;
import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.QueueReflexiveAbilityEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MSC", collectorNumber = "687")
public class ElektraFemmeFatale extends Card {

    public ElektraFemmeFatale() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), UnlessPaysEffect.youMayPay(DamageFromSourceCost.of(2),
                new QueueReflexiveAbilityEffect(DealDamageEffect.toTargetCreature(4))));
    }
}
