package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AlternativeCostForSpellsEffect;
import com.github.laxika.magicalvibes.model.effect.EnergyCountersEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DRC", collectorNumber = "13")
@CardRegistration(set = "DRC", collectorNumber = "29")
public class NissaWorldsoulSpeaker extends Card {

    public NissaWorldsoulSpeaker() {
        addTrigger(Triggers.enters(Triggers.LAND, ObjectRelation.YOU_CONTROL), new EnergyCountersEffect(2));
        addStaticEffect(AlternativeCostForSpellsEffect.payEnergy(8, new CardIsPermanentPredicate()));
    }
}
