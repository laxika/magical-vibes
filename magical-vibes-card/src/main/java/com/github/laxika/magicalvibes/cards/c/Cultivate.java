package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForBasicLandsToBattlefieldTappedAndHandEffect;

@CardRegistration(set = "M11", collectorNumber = "168")
public class Cultivate extends Card {

    public Cultivate() {
        addEffect(EffectSlot.SPELL, new SearchLibraryForBasicLandsToBattlefieldTappedAndHandEffect());
    }
}
