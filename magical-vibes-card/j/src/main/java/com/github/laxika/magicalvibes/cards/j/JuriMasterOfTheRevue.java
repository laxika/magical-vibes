package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MUL", collectorNumber = "46")
@CardRegistration(set = "MUL", collectorNumber = "111")
@CardRegistration(set = "MUL", collectorNumber = "176")
@CardRegistration(set = "EOC", collectorNumber = "119")
public class JuriMasterOfTheRevue extends Card {

    public JuriMasterOfTheRevue() {
        addTrigger(Triggers.sacrificed(null, ObjectRelation.YOU_CONTROL),
                PutCountersEffect.onSelf(CounterType.PLUS_ONE_PLUS_ONE));
        addTrigger(Triggers.dies(ObjectRelation.THIS),
                new DealDamageToAnyTargetEffect(new SourcePower()));
    }
}
