package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;

@CardRegistration(set = "ISD", collectorNumber = "73")
public class SelhoffOccultist extends Card {

    public SelhoffOccultist() {
        // Whenever Selhoff Occultist or another creature dies, target player mills a card.
        addEffect(EffectSlot.ON_DEATH, new MillEffect(1, MillRecipient.TARGET_PLAYER));
        addEffect(EffectSlot.ON_ANY_CREATURE_DIES, new MillEffect(1, MillRecipient.TARGET_PLAYER));
    }
}
