package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "5DN", collectorNumber = "53")
@CardRegistration(set = "SPG", collectorNumber = "4")
public class MephidrossVampire extends Card {

    public MephidrossVampire() {
        addStaticEffect(new GrantSubtypeEffect(CardSubtype.VAMPIRE, Subjects.creaturesYouControl()));
        addStaticEffect(new GrantTriggeredAbilityEffect(
                new TriggeredAbility(Triggers.dealsDamageToCreature(),
                PutCountersEffect.onSelf(CounterType.PLUS_ONE_PLUS_ONE)),
                Subjects.creaturesYouControl()));
    }
}
