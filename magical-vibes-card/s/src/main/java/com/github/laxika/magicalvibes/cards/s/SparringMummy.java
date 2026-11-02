package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "AKH", collectorNumber = "29")
public class SparringMummy extends Card {

    public SparringMummy() {
        // When this creature enters, untap target creature.
        target(TargetFilters.creature()).addTrigger(Triggers.enters(ObjectRelation.THIS),
                new UntapPermanentsEffect(TapUntapScope.TARGET));
    }
}
