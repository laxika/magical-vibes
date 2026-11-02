package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileCardsFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DFT", collectorNumber = "247")
public class WreckRemover extends Card {

    public WreckRemover() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new ExileCardsFromGraveyardEffect(1, 1));
        addTrigger(Triggers.attacks(), new ExileCardsFromGraveyardEffect(1, 1));
        addCycling("{2}");
    }
}
