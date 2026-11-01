package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "LGN", collectorNumber = "8")
public class DaruMender extends Card {

    public DaruMender() {
        addMorph("{W}");
        target(TargetFilters.creature()).addTrigger(Triggers.turnedFaceUp(null, ObjectRelation.THIS),
                new RegenerateEffect(true));
    }
}
