package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.TimesSourceMutated;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

@CardRegistration(set = "IKO", collectorNumber = "16")
public class HuntmasterLiger extends Card {

    public HuntmasterLiger() {
        addEffect(EffectSlot.ON_SELF_MUTATES, new BoostAllOwnCreaturesEffect(
                new TimesSourceMutated(),
                new TimesSourceMutated(),
                new PermanentNotPredicate(new PermanentIsSourcePermanentPredicate())));
    }
}
