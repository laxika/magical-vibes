package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ReturnDyingDamagedCreatureUnderControlEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TSB", collectorNumber = "47")
@CardRegistration(set = "SCG", collectorNumber = "74")
public class SoulCollector extends Card {

    public SoulCollector() {
        addMorph("{B}{B}{B}");
        addTrigger(Triggers.creatureDies(ObjectRelation.DAMAGED_BY_THIS),
                new ReturnDyingDamagedCreatureUnderControlEffect(null, null));
    }
}
