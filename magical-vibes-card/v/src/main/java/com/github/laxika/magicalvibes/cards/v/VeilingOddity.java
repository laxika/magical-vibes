package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.SourceCardSuspended;
import com.github.laxika.magicalvibes.model.effect.MakeAllCreaturesUnblockableEffect;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "PLC", collectorNumber = "51")
@CardRegistration(set = "TSR", collectorNumber = "95")
public class VeilingOddity extends Card {

    public VeilingOddity() {
        // When the last time counter is removed from this card while it's exiled, creatures can't
        // be blocked this turn. The negated suspend condition identifies the last counter event.
        addTrigger(Triggers.timeCounterRemovedFromThisInExile(),
                TriggerOptions.DEFAULT.withEventCondition(new NotCondition(new SourceCardSuspended())),
                new MakeAllCreaturesUnblockableEffect());

        addActivatedAbility(AbilityZones.HAND, new ActivatedAbility(
                false,
                "{1}{U}",
                List.of(),
                "Suspend 4\u2014{1}{U}",
                ActivationTimingRestriction.SORCERY_SPEED
        ).withSuspendsSourceFromHand(4));
    }
}
