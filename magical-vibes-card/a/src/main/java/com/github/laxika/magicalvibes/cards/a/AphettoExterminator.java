package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "LGN", collectorNumber = "59")
public class AphettoExterminator extends Card {

    public AphettoExterminator() {
        addMorph("{3}{B}");
        target(TargetFilters.creature())
                .addTrigger(Triggers.turnedFaceUp(null, ObjectRelation.THIS), new BoostTargetCreatureEffect(-3, -3));
    }
}
