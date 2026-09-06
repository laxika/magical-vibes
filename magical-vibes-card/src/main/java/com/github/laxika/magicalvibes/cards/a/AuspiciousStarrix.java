package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.TimesSourceMutated;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsUntilPermanentCountToBattlefieldEffect;

@CardRegistration(set = "IKO", collectorNumber = "144")
public class AuspiciousStarrix extends Card {

    public AuspiciousStarrix() {
        addEffect(EffectSlot.ON_SELF_MUTATES,
                new ExileTopCardsUntilPermanentCountToBattlefieldEffect(new TimesSourceMutated()));
    }
}
