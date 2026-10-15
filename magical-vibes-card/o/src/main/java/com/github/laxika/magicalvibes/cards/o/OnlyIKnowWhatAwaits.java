package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutPermanentCardsOfTypesFromHandEffect;

@CardRegistration(set = "DSC", collectorNumber = "350")
public class OnlyIKnowWhatAwaits extends Card {

    public OnlyIKnowWhatAwaits() {
        addEffect(EffectSlot.SPELL, new PutPermanentCardsOfTypesFromHandEffect());
    }
}
