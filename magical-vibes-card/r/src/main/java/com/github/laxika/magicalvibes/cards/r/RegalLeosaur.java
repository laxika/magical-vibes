package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

@CardRegistration(set = "IKO", collectorNumber = "202")
public class RegalLeosaur extends Card {

    public RegalLeosaur() {
        addEffect(EffectSlot.ON_SELF_MUTATES, new BoostAllOwnCreaturesEffect(
                2, 1, new PermanentNotPredicate(new PermanentIsSourcePermanentPredicate())));
    }
}
