package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.LoseLifeUnlessDiscardEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SOM", collectorNumber = "73")
@CardRegistration(set = "FDN", collectorNumber = "179")
@CardRegistration(set = "BRO", collectorNumber = "111")
public class PainfulQuandary extends Card {

    public PainfulQuandary() {
        addTrigger(Triggers.opponentCastsSpell().anySpell(), new LoseLifeUnlessDiscardEffect(5));
    }
}
