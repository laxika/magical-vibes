package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardsForEachCardTypeEffect;

@CardRegistration(set = "ONE", collectorNumber = "196")
public class AtraxaGrandUnifier extends Card {

    public AtraxaGrandUnifier() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new RevealTopCardsForEachCardTypeEffect(10));
    }
}
