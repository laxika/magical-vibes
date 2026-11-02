package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.Kicked;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.KickerEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "INV", collectorNumber = "5")
public class BenalishEmissary extends Card {

    public BenalishEmissary() {
        addStaticEffect(new KickerEffect("{1}{G}"));
        target(TargetFilters.land()).addTrigger(Triggers.enters(ObjectRelation.THIS),
                new ConditionalEffect(new Kicked(), new DestroyTargetPermanentEffect()));
    }
}
