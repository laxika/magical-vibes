package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.effect.AttachedBoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "CMM", collectorNumber = "41")
@CardRegistration(set = "ONC", collectorNumber = "78")
@CardRegistration(set = "ELD", collectorNumber = "314")
public class MaceOfTheValiant extends Card {

    public MaceOfTheValiant() {
        addStaticEffect(new AttachedBoostEffect(
                new CountersOnSource(CounterType.CHARGE),
                new CountersOnSource(CounterType.CHARGE),
                Subjects.attachedCreature()));
        addStaticEffect(new GrantKeywordEffect(Keyword.VIGILANCE, Subjects.attachedCreature()));
        addTrigger(Triggers.enters(Triggers.CREATURE, ObjectRelation.ANOTHER_YOU_CONTROL),
                PutCountersEffect.onSelf(CounterType.CHARGE));
        addActivatedAbility(new EquipActivatedAbility("{3}"));
    }
}
