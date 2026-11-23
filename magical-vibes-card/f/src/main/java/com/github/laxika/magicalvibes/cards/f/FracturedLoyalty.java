package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MRD", collectorNumber = "93")
public class FracturedLoyalty extends Card {

    public FracturedLoyalty() {
        target(TargetFilters.creature())
                .addTrigger(Triggers.becomesTargetOfSpellOrAbility(),
                        GainControlOfTargetEffect.ofTriggeringPermanent(Duration.PERMANENT)
                                .gainedBy(Subjects.controllerOf(Subjects.triggeringSpell())));
    }
}
