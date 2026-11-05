package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnPermanentControlledByPlayerToHandEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "PLC", collectorNumber = "19")
@CardRegistration(set = "DDN", collectorNumber = "53")
@CardRegistration(set = "TSR", collectorNumber = "45")
@CardRegistration(set = "C13", collectorNumber = "22")
@CardRegistration(set = "CMA", collectorNumber = "25")
public class Stonecloaker extends Card {

    public Stonecloaker() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new ReturnPermanentControlledByPlayerToHandEffect(
                        new PermanentIsCreaturePredicate(), "creature"));
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                ExileGraveyardCardsEffect.exactTargetedFromAnyGraveyard(1, null, false));
    }
}
