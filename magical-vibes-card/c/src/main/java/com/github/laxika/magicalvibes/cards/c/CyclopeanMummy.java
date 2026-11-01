package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileSourceCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "4ED", collectorNumber = "128")
@CardRegistration(set = "LEG", collectorNumber = "93")
@CardRegistration(set = "ME4", collectorNumber = "72")
public class CyclopeanMummy extends Card {

    public CyclopeanMummy() {
        // When this creature dies, exile it.
        addTrigger(Triggers.dies(ObjectRelation.THIS), new ExileSourceCardFromGraveyardEffect());
    }
}
