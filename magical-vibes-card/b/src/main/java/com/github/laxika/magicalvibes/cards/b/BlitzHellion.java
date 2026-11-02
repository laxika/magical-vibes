package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ShuffleSelfIntoOwnerLibraryEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ARB", collectorNumber = "49")
public class BlitzHellion extends Card {

    public BlitzHellion() {
        // Trample and haste are auto-loaded from Scryfall.
        // At the beginning of the end step, this creature's owner shuffles it into their library.
        addTrigger(Triggers.endStep(ActivePlayer.ANY), new ShuffleSelfIntoOwnerLibraryEffect());
    }
}
