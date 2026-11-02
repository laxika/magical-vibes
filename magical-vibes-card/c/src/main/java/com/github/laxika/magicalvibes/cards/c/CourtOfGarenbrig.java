package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.condition.ControllerIsMonarch;
import com.github.laxika.magicalvibes.model.effect.BecomeMonarchEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DistributeCountersAmongTargetsEffect;
import com.github.laxika.magicalvibes.model.effect.DoublePlusOneCountersOnControlledCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "WOC", collectorNumber = "25")
@CardRegistration(set = "WOC", collectorNumber = "33")
public class CourtOfGarenbrig extends Card {

    public CourtOfGarenbrig() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new BecomeMonarchEffect());

        target(TargetFilters.creature(), 0, 2).addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                SequenceEffect.of(
                        DistributeCountersAmongTargetsEffect.evenlyAmongTargets(
                                CounterType.PLUS_ONE_PLUS_ONE, 2),
                        new ConditionalEffect(new ControllerIsMonarch(),
                                new DoublePlusOneCountersOnControlledCreaturesEffect())));
    }
}
