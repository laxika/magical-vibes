package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.TimesSourceMutated;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "IKO", collectorNumber = "16")
public class HuntmasterLiger extends Card {

    public HuntmasterLiger() {
        addTrigger(Triggers.mutates(), BoostEffect.creaturesYouControl(
                new TimesSourceMutated(),
                new TimesSourceMutated(),
                new PermanentNotPredicate(new PermanentIsSourcePermanentPredicate())));
    }
}
