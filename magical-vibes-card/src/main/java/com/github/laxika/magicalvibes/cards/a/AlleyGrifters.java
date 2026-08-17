package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;

@CardRegistration(set = "MMQ", collectorNumber = "115")
public class AlleyGrifters extends Card {

    public AlleyGrifters() {
        // Whenever this creature becomes blocked, defending player discards a card.
        addEffect(EffectSlot.ON_BECOMES_BLOCKED,
                new DiscardEffect(1, DiscardRecipient.DEFENDING_PLAYER));
    }
}
