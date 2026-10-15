package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ShuffleControllerGraveyardIntoLibraryThenMillTargetEffect;

@CardRegistration(set = "RTR", collectorNumber = "47")
public class PsychicSpiral extends Card {

    public PsychicSpiral() {
        // The mill count is the number of cards shuffled out of YOUR graveyard, captured by the
        // effect between the shuffle and the mill.
        addEffect(EffectSlot.SPELL, new ShuffleControllerGraveyardIntoLibraryThenMillTargetEffect());
    }
}
