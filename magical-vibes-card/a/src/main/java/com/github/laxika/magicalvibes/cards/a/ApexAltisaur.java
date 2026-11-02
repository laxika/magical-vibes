package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SourceFightsTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "LCC", collectorNumber = "232")
@CardRegistration(set = "C19", collectorNumber = "31")
public class ApexAltisaur extends Card {

    public ApexAltisaur() {
        target(TargetFilters.creatureAnOpponentControls(), 0, 1)
                .addTrigger(Triggers.enters(ObjectRelation.THIS), new SourceFightsTargetCreatureEffect())
                .addTrigger(Triggers.dealtDamage(), new SourceFightsTargetCreatureEffect());
    }
}
