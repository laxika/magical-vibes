package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "EVE", collectorNumber = "1")
@CardRegistration(set = "M12", collectorNumber = "6")
public class ArchonOfJustice extends Card {

    public ArchonOfJustice() {
        // When this creature dies, exile target permanent.
        target(TargetFilters.permanent()).addTrigger(Triggers.dies(ObjectRelation.THIS), new ExileTargetPermanentEffect());
    }
}
