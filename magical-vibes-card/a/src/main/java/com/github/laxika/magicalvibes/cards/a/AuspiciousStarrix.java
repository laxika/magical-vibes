package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.TimesSourceMutated;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsUntilPermanentCountToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "IKO", collectorNumber = "144")
public class AuspiciousStarrix extends Card {

    public AuspiciousStarrix() {
        addTrigger(Triggers.mutates(),
                new ExileTopCardsUntilPermanentCountToBattlefieldEffect(new TimesSourceMutated()));
    }
}
