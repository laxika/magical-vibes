package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyAttachmentsOnTargetCreatureEffect;

@CardRegistration(set = "SHM", collectorNumber = "24")
public class StripBare extends Card {

    public StripBare() {
        addEffect(EffectSlot.SPELL, new DestroyAttachmentsOnTargetCreatureEffect(true, true));
    }
}
