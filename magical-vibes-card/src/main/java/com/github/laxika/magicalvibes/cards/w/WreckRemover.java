package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileCardsFromGraveyardEffect;

@CardRegistration(set = "DFT", collectorNumber = "247")
public class WreckRemover extends Card {

    public WreckRemover() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ExileCardsFromGraveyardEffect(1, 1));
        addEffect(EffectSlot.ON_ATTACK, new ExileCardsFromGraveyardEffect(1, 1));
        addCycling("{2}");
    }
}
