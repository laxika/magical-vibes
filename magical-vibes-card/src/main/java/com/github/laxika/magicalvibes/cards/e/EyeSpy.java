package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LookAtTargetPlayerTopCardMayGraveyardEffect;

@CardRegistration(set = "P02", collectorNumber = "39")
public class EyeSpy extends Card {

    public EyeSpy() {
        // Look at the top card of target player's library. You may put that card into their graveyard.
        addEffect(EffectSlot.SPELL, new LookAtTargetPlayerTopCardMayGraveyardEffect());
    }
}
