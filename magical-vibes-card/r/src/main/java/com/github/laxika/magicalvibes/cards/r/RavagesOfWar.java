package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "PTK", collectorNumber = "17")
@CardRegistration(set = "ME2", collectorNumber = "27")
@CardRegistration(set = "PIP", collectorNumber = "354")
@CardRegistration(set = "PIP", collectorNumber = "882")
public class RavagesOfWar extends Card {

    public RavagesOfWar() {
        addSpellEffect(new DestroyAllPermanentsEffect(new PermanentIsLandPredicate()));
    }
}
