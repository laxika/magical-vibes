package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SkipNextUntapEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "AFR", collectorNumber = "56")
public class DragonTurtle extends Card {

    public DragonTurtle() {
        // Flash is loaded from Scryfall.

        // When this creature enters, tap it and up to one target creature an opponent controls.
        // They don't untap during their controllers' next untap steps.
        target(TargetFilters.creatureAnOpponentControls(), 0, 1)
                .addTrigger(Triggers.enters(ObjectRelation.THIS), new TapPermanentsEffect(TapUntapScope.SELF))
                .addTrigger(Triggers.enters(ObjectRelation.THIS), new TapPermanentsEffect(TapUntapScope.TARGET))
                .addTrigger(Triggers.enters(ObjectRelation.THIS), new SkipNextUntapEffect(TapUntapScope.SELF))
                .addTrigger(Triggers.enters(ObjectRelation.THIS), new SkipNextUntapEffect(TapUntapScope.TARGET));
    }
}
