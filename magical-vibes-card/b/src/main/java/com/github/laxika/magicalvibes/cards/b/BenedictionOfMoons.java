package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.HauntEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "GPT", collectorNumber = "3")
public class BenedictionOfMoons extends Card {

    public BenedictionOfMoons() {
        addSpellEffect(new GainLifeEffect(2));
        target(TargetFilters.creature()).addTrigger(Triggers.putIntoGraveyardWhileResolving(ObjectRelation.THIS), new HauntEffect());
        addEffect(EffectSlot.ON_HAUNTED_CREATURE_DIES, new GainLifeEffect(2));
    }
}
