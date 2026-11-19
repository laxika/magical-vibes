package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DKA", collectorNumber = "15")
public class NiblisOfTheMist extends Card {

    public NiblisOfTheMist() {
        // Flying is loaded from Scryfall.
        // When this creature enters, you may tap target creature.
        target(TargetFilters.creature()).addTrigger(Triggers.enters(ObjectRelation.THIS), new MayEffect(
                new TapPermanentsEffect(Subjects.target()),
                "Tap target creature?"
        ));
    }
}
