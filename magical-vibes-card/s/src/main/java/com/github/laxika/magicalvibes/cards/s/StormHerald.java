package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ReturnAurasFromGraveyardAttachedToCreaturesEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "THB", collectorNumber = "156")
@CardRegistration(set = "THB", collectorNumber = "321")
public class StormHerald extends Card {

    public StormHerald() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new ReturnAurasFromGraveyardAttachedToCreaturesEffect());
    }
}
