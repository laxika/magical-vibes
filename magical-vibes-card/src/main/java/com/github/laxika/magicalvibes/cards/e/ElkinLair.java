package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileRandomCardFromHandMayPlayThisTurnEffect;

@CardRegistration(set = "VIS", collectorNumber = "78")
public class ElkinLair extends Card {

    public ElkinLair() {
        // EACH_UPKEEP_TRIGGERED sets targetId to the active player. That player exiles a random
        // hand card, may play it this turn; unplayed → graveyard at next end step.
        // World rule is handled by SBA.
        addEffect(EffectSlot.EACH_UPKEEP_TRIGGERED, new ExileRandomCardFromHandMayPlayThisTurnEffect());
    }
}
