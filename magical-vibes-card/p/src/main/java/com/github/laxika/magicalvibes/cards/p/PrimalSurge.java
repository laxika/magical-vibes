package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayPutPermanentOntoBattlefieldRepeatEffect;

@CardRegistration(set = "AVR", collectorNumber = "189")
public class PrimalSurge extends Card {

    public PrimalSurge() {
        // Exile the top card of your library. If it's a permanent card, you may put it onto the
        // battlefield. If you do, repeat this process.
        addEffect(EffectSlot.SPELL, new ExileTopCardMayPutPermanentOntoBattlefieldRepeatEffect());
    }
}
