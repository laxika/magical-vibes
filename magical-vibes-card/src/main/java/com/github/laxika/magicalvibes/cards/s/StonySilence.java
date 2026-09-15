package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ActivatedAbilitiesOfMatchingPermanentsCantBeActivatedEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

@CardRegistration(set = "ISD", collectorNumber = "36")
@CardRegistration(set = "MM3", collectorNumber = "25")
public class StonySilence extends Card {

    public StonySilence() {
        addEffect(EffectSlot.STATIC, new ActivatedAbilitiesOfMatchingPermanentsCantBeActivatedEffect(
                new PermanentIsArtifactPredicate()
        ));
    }
}
