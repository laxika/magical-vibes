package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileTopUntilNonlandPermanentToBattlefieldOrHandEffect;

@CardRegistration(set = "IKO", collectorNumber = "190")
public class IllunaApexOfWishes extends Card {

    public IllunaApexOfWishes() {
        addEffect(EffectSlot.ON_SELF_MUTATES,
                new ExileTopUntilNonlandPermanentToBattlefieldOrHandEffect());
    }
}
