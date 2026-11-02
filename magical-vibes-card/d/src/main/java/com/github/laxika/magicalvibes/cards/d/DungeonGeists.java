package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DoesntUntapEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;


@CardRegistration(set = "DKA", collectorNumber = "36")
@CardRegistration(set = "M20", collectorNumber = "57")
@CardRegistration(set = "C13", collectorNumber = "42")
@CardRegistration(set = "CMA", collectorNumber = "39")
public class DungeonGeists extends Card {

    public DungeonGeists() {
        // Flying is loaded from Scryfall.

        // When this creature enters, tap target creature an opponent controls. That creature
        // doesn't untap during its controller's untap step for as long as you control this creature.
        target(TargetFilters.creatureAnOpponentControls())
                .addTrigger(Triggers.enters(ObjectRelation.THIS), new TapPermanentsEffect(TapUntapScope.TARGET))
                .addTrigger(Triggers.enters(ObjectRelation.THIS), DoesntUntapEffect.targetWhileSourceControlled());
    }
}
