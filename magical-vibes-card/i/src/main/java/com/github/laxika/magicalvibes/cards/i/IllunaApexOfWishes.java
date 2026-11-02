package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileTopUntilNonlandPermanentToBattlefieldOrHandEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "IKO", collectorNumber = "190")
@CardRegistration(set = "DMC", collectorNumber = "154")
public class IllunaApexOfWishes extends Card {

    public IllunaApexOfWishes() {
        addTrigger(Triggers.mutates(),
                new ExileTopUntilNonlandPermanentToBattlefieldOrHandEffect());
    }
}
