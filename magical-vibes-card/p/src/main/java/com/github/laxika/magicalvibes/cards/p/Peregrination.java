package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForBasicLandsToBattlefieldTappedAndHandEffect;

@CardRegistration(set = "BNG", collectorNumber = "132")
public class Peregrination extends Card {

    public Peregrination() {
        addSpellEffect(new SearchLibraryForBasicLandsToBattlefieldTappedAndHandEffect());
        addSpellEffect(new ScryEffect(1));
    }
}
