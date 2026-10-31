package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PutTargetPermanentIntoLibraryNFromTopOrBottomEffect;
import com.github.laxika.magicalvibes.model.effect.SurveilEffect;

@CardRegistration(set = "DSK", collectorNumber = "82")
public class VanishFromSight extends Card {

    public VanishFromSight() {
        addSpellEffect(new PutTargetPermanentIntoLibraryNFromTopOrBottomEffect(0));
        addSpellEffect(new SurveilEffect(1));
    }
}
