package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PsychicTransferEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "6ED", collectorNumber = "90")
@CardRegistration(set = "MIR", collectorNumber = "85")
@CardRegistration(set = "S99", collectorNumber = "46")
public class PsychicTransfer extends Card {

    public PsychicTransfer() {
        addEffect(EffectSlot.SPELL, new PsychicTransferEffect());
    }
}
