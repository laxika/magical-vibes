package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedStepTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "ICE", collectorNumber = "314")
@CardRegistration(set = "ME4", collectorNumber = "188")
public class CelestialSword extends Card {

    public CelestialSword() {
        addActivatedAbility(new ActivatedAbility(
                true, "{3}",
                List.of(
                        BoostEffect.target(3, 3),
                        RegisterDelayedStepTriggerEffect.aboutAtNext(Triggers.endStep(ActivePlayer.ANY),
                                Subjects.target(),
                                SacrificeSelfEffect.ofTriggeringPermanent(true, null))
                ),
                "{3}, {T}: Target creature you control gets +3/+3 until end of turn. "
                        + "Its controller sacrifices it at the beginning of the next end step.",
                TargetFilters.creatureYouControl()
        ));
    }
}
