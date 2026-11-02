package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.DuringYourUntapStep;
import com.github.laxika.magicalvibes.model.effect.AdditionalUpkeepStepEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "WHO", collectorNumber = "148")
@CardRegistration(set = "WHO", collectorNumber = "432")
@CardRegistration(set = "WHO", collectorNumber = "560")
@CardRegistration(set = "WHO", collectorNumber = "753")
@CardRegistration(set = "WHO", collectorNumber = "1023")
@CardRegistration(set = "WHO", collectorNumber = "1151")
public class TheNinthDoctor extends Card {

    public TheNinthDoctor() {
        // Into the TARDIS — Whenever The Ninth Doctor becomes untapped during your untap step, you get
        // an additional upkeep step after this step.
        addTrigger(Triggers.becomesUntapped(), ConditionalEffect.atTriggerTime(
                new DuringYourUntapStep(), new AdditionalUpkeepStepEffect()));
    }
}
