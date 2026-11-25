package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.AttachedBoostEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "M21", collectorNumber = "112")
public class MaleficScythe extends Card {

    public MaleficScythe() {
        addStaticEffect(new EnterWithCountersEffect(CounterType.SOUL, new Fixed(1)));
        addStaticEffect(new AttachedBoostEffect(
                new CountersOnSource(CounterType.SOUL),
                new CountersOnSource(CounterType.SOUL),
                Subjects.attachedCreature()));
        addTrigger(Triggers.creatureDies(ObjectRelation.EQUIPPED),
                PutCountersEffect.onSelf(CounterType.SOUL));
        addActivatedAbility(new EquipActivatedAbility("{1}"));
    }
}
