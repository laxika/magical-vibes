package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseSubtypeOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSourceChosenSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SLD", collectorNumber = "2369")
@CardRegistration(set = "C20", collectorNumber = "47")
public class SpeciesSpecialist extends Card {

    public SpeciesSpecialist() {
        addStaticEffect(new ChooseSubtypeOnEnterEffect());

        var deathTrigger = new TriggeringPermanentConditionalEffect(
                new PermanentHasSourceChosenSubtypePredicate(),
                new MayEffect(new DrawCardEffect(), "Draw a card?"));
        addTrigger(Triggers.dies(ObjectRelation.THIS), deathTrigger);
        addEffect(EffectSlot.ON_ANY_CREATURE_DIES, deathTrigger);
    }
}
