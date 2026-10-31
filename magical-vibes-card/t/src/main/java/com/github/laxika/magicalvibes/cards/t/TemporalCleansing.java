package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PutTargetPermanentIntoLibraryNFromTopOrBottomEffect;

@CardRegistration(set = "ECL", collectorNumber = "78")
@CardRegistration(set = "MOM", collectorNumber = "80")
public class TemporalCleansing extends Card {

    public TemporalCleansing() {
        addSpellEffect(new PutTargetPermanentIntoLibraryNFromTopOrBottomEffect(1));
    }
}
