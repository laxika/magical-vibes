package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedIfDefenderControlsMatchingPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

@CardRegistration(set = "ULG", collectorNumber = "28")
public class BouncingBeebles extends Card {

    public BouncingBeebles() {
        addStaticEffect(new CantBeBlockedIfDefenderControlsMatchingPermanentEffect(
                new PermanentIsArtifactPredicate()
        ));
    }
}
