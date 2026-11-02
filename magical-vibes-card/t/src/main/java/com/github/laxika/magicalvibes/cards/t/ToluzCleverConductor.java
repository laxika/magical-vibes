package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawDiscardAndConniveEffect;
import com.github.laxika.magicalvibes.model.effect.ExileDiscardedCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.PutAllCardsExiledWithSourceIntoOwnersHandsEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SNC", collectorNumber = "228")
public class ToluzCleverConductor extends Card {

    public ToluzCleverConductor() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new DrawDiscardAndConniveEffect());
        addTrigger(Triggers.youDiscardOneOrMoreCards(), new ExileDiscardedCardFromGraveyardEffect(true));
        addTrigger(Triggers.dies(ObjectRelation.THIS), new PutAllCardsExiledWithSourceIntoOwnersHandsEffect());
    }
}
