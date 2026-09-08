package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.RashmiTriggerEffect;

@CardRegistration(set = "KLD", collectorNumber = "184")
public class RashmiEternitiesCrafter extends Card {

    public RashmiEternitiesCrafter() {
        // Whenever you cast your first spell each turn, reveal the top card of your library and
        // use the triggering spell's mana value to determine whether it may be cast for free.
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new RashmiTriggerEffect());
    }
}
