package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.EnterBattlefieldOnDiscardEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "APC", collectorNumber = "134")
@CardRegistration(set = "TSB", collectorNumber = "108")
@CardRegistration(set = "DMR", collectorNumber = "220")
public class Dodecapod extends Card {

    public Dodecapod() {
        addTrigger(Triggers.opponentCausesYouToDiscardThisCard(),
                new EnterBattlefieldOnDiscardEffect(CounterType.PLUS_ONE_PLUS_ONE, 2));
    }
}
