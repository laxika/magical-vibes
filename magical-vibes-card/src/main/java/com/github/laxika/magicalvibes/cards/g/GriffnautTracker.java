package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileCardsFromGraveyardEffect;

@CardRegistration(set = "MKM", collectorNumber = "17")
public class GriffnautTracker extends Card {

    public GriffnautTracker() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ExileCardsFromGraveyardEffect(2, 0, true));
    }
}
