package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;

@CardRegistration(set = "MH1", collectorNumber = "103")
public class RansackTheLab extends Card {

    public RansackTheLab() {
        // Look at the top three cards of your library. Put one into your hand and the rest into your graveyard.
        addEffect(EffectSlot.SPELL, LookAtTopCardsEffect.chooseNToHandRestToGraveyard(3, 1));
    }
}
