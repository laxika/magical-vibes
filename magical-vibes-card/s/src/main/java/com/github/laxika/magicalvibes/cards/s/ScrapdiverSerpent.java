package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedIfDefenderControlsMatchingPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "SOM", collectorNumber = "41")
public class ScrapdiverSerpent extends Card {

    public ScrapdiverSerpent() {
        addStaticEffect(new CantBeBlockedIfDefenderControlsMatchingPermanentEffect(
                new PermanentIsArtifactPredicate()
        ));
    }
}
