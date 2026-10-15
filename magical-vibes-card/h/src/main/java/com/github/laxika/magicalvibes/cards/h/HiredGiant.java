package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EachOpponentMaySearchLibraryForLandToBattlefieldEffect;

@CardRegistration(set = "MMQ", collectorNumber = "194")
public class HiredGiant extends Card {

    public HiredGiant() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new EachOpponentMaySearchLibraryForLandToBattlefieldEffect());
    }
}
