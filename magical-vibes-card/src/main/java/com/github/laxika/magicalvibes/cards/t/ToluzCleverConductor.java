package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawDiscardAndConniveEffect;
import com.github.laxika.magicalvibes.model.effect.ExileDiscardedCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.PutAllCardsExiledWithSourceIntoOwnersHandsEffect;

@CardRegistration(set = "SNC", collectorNumber = "228")
public class ToluzCleverConductor extends Card {

    public ToluzCleverConductor() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DrawDiscardAndConniveEffect());
        addEffect(EffectSlot.ON_CONTROLLER_DISCARDS, new ExileDiscardedCardFromGraveyardEffect(true));
        addEffect(EffectSlot.ON_DEATH, new PutAllCardsExiledWithSourceIntoOwnersHandsEffect());
    }
}
