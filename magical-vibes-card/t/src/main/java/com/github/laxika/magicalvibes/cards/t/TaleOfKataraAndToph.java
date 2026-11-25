package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TLE", collectorNumber = "143")
@CardRegistration(set = "TLE", collectorNumber = "207")
public class TaleOfKataraAndToph extends Card {

    public TaleOfKataraAndToph() {
        addStaticEffect(new GrantTriggeredAbilityEffect(
                new TriggeredAbility(Triggers.becomesTappedForFirstTimeDuringYourTurn(),
                        PutCountersEffect.onSelf(CounterType.PLUS_ONE_PLUS_ONE)),
                Subjects.creaturesYouControl()));
    }
}
