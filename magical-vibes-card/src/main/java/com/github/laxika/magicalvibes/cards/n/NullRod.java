package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ActivatedAbilitiesOfMatchingPermanentsCantBeActivatedEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

@CardRegistration(set = "WTH", collectorNumber = "154")
public class NullRod extends Card {

    public NullRod() {
        addEffect(EffectSlot.STATIC, new ActivatedAbilitiesOfMatchingPermanentsCantBeActivatedEffect(
                new PermanentIsArtifactPredicate()
        ));
    }
}
