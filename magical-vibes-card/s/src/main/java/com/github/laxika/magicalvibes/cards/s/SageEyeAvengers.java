package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.TargetPermanentMatches;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerLessThanSourcePowerPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;


@CardRegistration(set = "FRF", collectorNumber = "50")
@CardRegistration(set = "DDS", collectorNumber = "8")
public class SageEyeAvengers extends Card {

    public SageEyeAvengers() {
        target(TargetFilters.creature()).addTrigger(Triggers.attacks(),
                new ConditionalEffect(
                        new TargetPermanentMatches(new PermanentPowerLessThanSourcePowerPredicate()),
                        new MayEffect(ReturnToHandEffect.target(),
                                "Return target creature to its owner's hand?")));
    }
}
