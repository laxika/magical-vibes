package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.effect.AttachedBoostEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "FIN", collectorNumber = "257")
@CardRegistration(set = "FIN", collectorNumber = "352")
public class ExcaliburII extends Card {

    public ExcaliburII() {
        addTrigger(Triggers.youGainLife(),
                PutCountersEffect.onSelf(CounterType.CHARGE));
        addStaticEffect(new AttachedBoostEffect(
                new CountersOnSource(CounterType.CHARGE),
                new CountersOnSource(CounterType.CHARGE),
                Subjects.attachedCreature()));
        addActivatedAbility(new EquipActivatedAbility("{3}"));
    }
}
