package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedEndStepTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeTargetPermanentThenDealPowerDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "YDFT", collectorNumber = "13")
public class GoblinCrashPilot extends Card {

    public GoblinCrashPilot() {
        addTrigger(Triggers.crewsVehicle(), SequenceEffect.of(
                new GrantKeywordEffect(Keyword.HASTE, GrantScope.TRIGGERING_PERMANENT),
                new RegisterDelayedEndStepTriggerEffect(
                        List.of(), new SacrificeTargetPermanentThenDealPowerDamageToAnyTargetEffect())));
    }
}
