package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "40K", collectorNumber = "152")
public class CanoptekTombSentinel extends Card {

    public CanoptekTombSentinel() {
        target(TargetFilters.nonlandPermanent(), 0, 1)
                .addTrigger(Triggers.entersFrom(Zone.GRAVEYARD, null, ObjectRelation.THIS),
                        new ExileTargetPermanentEffect());

        addUnearth("{7}");
    }
}
