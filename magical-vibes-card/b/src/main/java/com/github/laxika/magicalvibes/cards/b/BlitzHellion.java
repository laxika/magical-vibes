package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ShuffleSelfIntoOwnerLibraryEffect;

@CardRegistration(set = "ARB", collectorNumber = "49")
public class BlitzHellion extends Card {

    public BlitzHellion() {
        // Trample and haste are auto-loaded from Scryfall.
        // At the beginning of the end step, this creature's owner shuffles it into their library.
        addEffect(EffectSlot.END_STEP_TRIGGERED, new ShuffleSelfIntoOwnerLibraryEffect());
    }
}
