package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TLE", collectorNumber = "143")
@CardRegistration(set = "TLE", collectorNumber = "207")
public class TaleOfKataraAndToph extends Card {

    public TaleOfKataraAndToph() {
        addStaticEffect(new GrantTriggeredAbilityEffect(
                new TriggeredAbility(Triggers.becomesTappedForFirstTimeDuringYourTurn(),
                        new PutCountersOnSourceEffect(1, 1, 1)),
                GrantScope.ALL_OWN_CREATURES));
    }
}
