package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "PIP", collectorNumber = "81")
@CardRegistration(set = "PIP", collectorNumber = "400")
@CardRegistration(set = "PIP", collectorNumber = "609")
@CardRegistration(set = "PIP", collectorNumber = "928")
public class PowerFist extends Card {

    public PowerFist() {
        addStaticEffect(new GrantKeywordEffect(Keyword.TRAMPLE, Subjects.attachedCreature()));
        addStaticEffect(new GrantTriggeredAbilityEffect(
                new TriggeredAbility(Triggers.dealsCombatDamageToPlayer(),
                PutCountersEffect.onSelf(CounterType.PLUS_ONE_PLUS_ONE, new EventValue())),
                Subjects.attachedCreature()));
        addActivatedAbility(new EquipActivatedAbility("{2}"));
    }
}
