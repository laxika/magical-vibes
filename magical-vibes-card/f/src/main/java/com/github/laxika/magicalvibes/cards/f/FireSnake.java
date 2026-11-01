package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "POR", collectorNumber = "127")
public class FireSnake extends Card {

    public FireSnake() {
        // When this creature dies, destroy target land.
        target(TargetFilters.land()).addTrigger(Triggers.dies(ObjectRelation.THIS), new DestroyTargetPermanentEffect());
    }
}
