package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForBasicLandsToBattlefieldTappedAndHandEffect;

@CardRegistration(set = "BNG", collectorNumber = "132")
public class Peregrination extends Card {

    public Peregrination() {
        addEffect(EffectSlot.SPELL, new SearchLibraryForBasicLandsToBattlefieldTappedAndHandEffect());
        addEffect(EffectSlot.SPELL, new ScryEffect(1));
    }
}
