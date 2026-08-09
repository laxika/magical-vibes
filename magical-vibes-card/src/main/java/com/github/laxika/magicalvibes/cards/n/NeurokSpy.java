package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedIfDefenderControlsMatchingPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

@CardRegistration(set = "MRD", collectorNumber = "44")
public class NeurokSpy extends Card {

    public NeurokSpy() {
        addEffect(EffectSlot.STATIC, new CantBeBlockedIfDefenderControlsMatchingPermanentEffect(
                new PermanentIsArtifactPredicate()
        ));
    }
}
