package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileCardsFromGraveyardEffect;

@CardRegistration(set = "INR", collectorNumber = "40")
public class SoulGuideGryff extends Card {

    public SoulGuideGryff() {
        // When this creature enters, exile up to one target card from a graveyard.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ExileCardsFromGraveyardEffect(1, 0));
    }
}
