package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutControlledCreatureOnTopOfLibraryEffect;

@CardRegistration(set = "ARB", collectorNumber = "102")
public class NulltreadGargantuan extends Card {

    public NulltreadGargantuan() {
        // When this creature enters, put a creature you control on top of its owner's library.
        // Non-targeted: the creature is chosen as the ability resolves (it may be this creature
        // itself if it is the only creature you control).
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new PutControlledCreatureOnTopOfLibraryEffect());
    }
}
