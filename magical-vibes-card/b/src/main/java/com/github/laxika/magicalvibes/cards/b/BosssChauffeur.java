package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;
import com.github.laxika.magicalvibes.model.amount.CountersOnTriggeringPermanent;

@CardRegistration(set = "NCC", collectorNumber = "13")
@CardRegistration(set = "NCC", collectorNumber = "114")
@CardRegistration(set = "BLC", collectorNumber = "137")
public class BosssChauffeur extends Card {

    public BosssChauffeur() {
        PermanentCount otherCreatures = new PermanentCount(
                new PermanentIsCreaturePredicate(), CountScope.CONTROLLER, true);
        addStaticEffect(new EnterWithCountersEffect(
                CounterType.PLUS_ONE_PLUS_ONE, new Sum(new Fixed(1), otherCreatures)));
        addTrigger(Triggers.enters(Triggers.CREATURE, ObjectRelation.ANOTHER_YOU_CONTROL),
                PutCountersEffect.onSelf(CounterType.PLUS_ONE_PLUS_ONE));
        addTrigger(Triggers.dies(ObjectRelation.THIS), new CreateTokenEffect("Citizen", 1, 1, CardColor.GREEN,
                        Set.of(CardColor.GREEN, CardColor.WHITE), List.of(CardSubtype.CITIZEN))
                                .withAmount(new CountersOnTriggeringPermanent(CounterType.PLUS_ONE_PLUS_ONE)));
    }
}
