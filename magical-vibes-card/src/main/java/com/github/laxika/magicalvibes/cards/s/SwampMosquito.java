package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GivePoisonCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PoisonRecipient;

@CardRegistration(set = "ALL", collectorNumber = "63a")
@CardRegistration(set = "ALL", collectorNumber = "63b")
@CardRegistration(set = "TSB", collectorNumber = "49")
public class SwampMosquito extends Card {

    public SwampMosquito() {
        // Whenever this creature attacks and isn't blocked, defending player gets a poison counter.
        addEffect(EffectSlot.ON_ATTACKS_UNBLOCKED, new GivePoisonCountersEffect(1, PoisonRecipient.TARGET_PLAYER));
    }
}
