package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MillHalfLibraryEffect;

@CardRegistration(set = "XLN", collectorNumber = "57")
public class FleetSwallower extends Card {

    public FleetSwallower() {
        // Whenever Fleet Swallower attacks, target player mills half their library, rounded up.
        addEffect(EffectSlot.ON_ATTACK, new MillHalfLibraryEffect(true));
    }
}
