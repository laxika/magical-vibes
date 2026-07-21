package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MillDefendingPlayerEffect;

@CardRegistration(set = "ARB", collectorNumber = "28")
public class NemesisOfReason extends Card {

    public NemesisOfReason() {
        // Whenever Nemesis of Reason attacks, defending player mills ten cards.
        addEffect(EffectSlot.ON_ATTACK, new MillDefendingPlayerEffect(10));
    }
}
