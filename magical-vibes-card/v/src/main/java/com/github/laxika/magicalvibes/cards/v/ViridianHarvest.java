package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "NPH", collectorNumber = "125")
public class ViridianHarvest extends Card {

    public ViridianHarvest() {
        // When enchanted artifact is put into a graveyard, you gain 6 life.
        target(TargetFilters.artifact()).addTrigger(Triggers.dies(ObjectRelation.ENCHANTED), new GainLifeEffect(6));
    }
}
