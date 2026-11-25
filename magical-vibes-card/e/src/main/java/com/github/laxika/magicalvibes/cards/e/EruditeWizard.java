package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "FDN", collectorNumber = "37")
public class EruditeWizard extends Card {

    public EruditeWizard() {
        addTrigger(Triggers.youDrawSecondCardEachTurn(),
                PutCountersEffect.onSelf(CounterType.PLUS_ONE_PLUS_ONE));
    }
}
