package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForBasicLandsToBattlefieldTappedAndHandEffect;

@CardRegistration(set = "ACR", collectorNumber = "43")
public class ViewpointSynchronization extends Card {

    public ViewpointSynchronization() {
        addEffect(EffectSlot.SPELL,
                SearchLibraryForBasicLandsToBattlefieldTappedAndHandEffect.twoToBattlefieldTapped());
    }
}
