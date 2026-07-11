package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "PTK", collectorNumber = "89")
public class WeiNightRaiders extends Card {

    public WeiNightRaiders() {
        // Whenever this creature deals damage to an opponent, that player discards a card.
        addEffect(EffectSlot.ON_DAMAGE_TO_PLAYER, new DiscardEffect(1, DiscardRecipient.TARGET_PLAYER));
    }
}
