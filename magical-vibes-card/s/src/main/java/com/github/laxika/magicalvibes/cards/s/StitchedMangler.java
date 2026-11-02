package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;
import com.github.laxika.magicalvibes.model.effect.SkipNextUntapEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;


@CardRegistration(set = "INR", collectorNumber = "87")
@CardRegistration(set = "SOI", collectorNumber = "89")
public class StitchedMangler extends Card {

    public StitchedMangler() {
        // This creature enters tapped.
        addStaticEffect(new EntersTappedEffect());

        // When this creature enters, tap target creature an opponent controls.
        // That creature doesn't untap during its controller's next untap step.
        target(TargetFilters.creatureAnOpponentControls())
                .addTrigger(Triggers.enters(ObjectRelation.THIS), new TapPermanentsEffect(TapUntapScope.TARGET))
                .addTrigger(Triggers.enters(ObjectRelation.THIS), new SkipNextUntapEffect(TapUntapScope.TARGET));
    }
}
