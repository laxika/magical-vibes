package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.HauntEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "GPT", collectorNumber = "3")
public class BenedictionOfMoons extends Card {

    public BenedictionOfMoons() {
        addSpellEffect(new GainLifeEffect(2));
        target(TargetFilters.creature()).addTrigger(Triggers.putIntoGraveyardWhileResolving(ObjectRelation.THIS),
                new HauntEffect());
        addTrigger(Triggers.dies(ObjectRelation.HAUNTED), new GainLifeEffect(2));
    }
}
