package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DiscardHandEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;

@CardRegistration(set = "DGM", collectorNumber = "104")
public class SireOfInsanity extends Card {

    public SireOfInsanity() {
        // At the beginning of each end step, each player discards their hand.
        addEffect(EffectSlot.END_STEP_TRIGGERED, new DiscardHandEffect(DiscardRecipient.EACH_PLAYER));
    }
}
