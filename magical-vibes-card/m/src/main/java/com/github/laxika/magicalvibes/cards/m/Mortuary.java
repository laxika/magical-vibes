package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PutTriggeringCardFromGraveyardOnTopOfLibraryEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "STH", collectorNumber = "66")
@CardRegistration(set = "SLZ", collectorNumber = "47")
@CardRegistration(set = "SLZ", collectorNumber = "168")
@CardRegistration(set = "SLZ", collectorNumber = "289")
public class Mortuary extends Card {

    public Mortuary() {
        // Whenever a creature is put into your graveyard from the battlefield, put that card on
        // top of your library.
        addTrigger(Triggers.creatureDies(ObjectRelation.YOU_OWN),
                new PutTriggeringCardFromGraveyardOnTopOfLibraryEffect());
    }
}
