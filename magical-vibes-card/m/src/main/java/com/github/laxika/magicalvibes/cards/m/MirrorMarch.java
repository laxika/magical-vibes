package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.StepKind;
import java.util.Set;
import java.util.Map;
import java.util.List;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.FlipUntilLoseEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "RNA", collectorNumber = "108")
public class MirrorMarch extends Card {

    public MirrorMarch() {
        addTrigger(Triggers.enters(Triggers.NONTOKEN_CREATURE, ObjectRelation.ANOTHER_YOU_CONTROL),
                FlipUntilLoseEffect.untilLose(),
                new CreateTokenCopyOfTargetPermanentEffect(List.of(), Set.of(), null, null, Map.of(), true, true,
                        false, false, false, false, null, Set.of(), false, List.of(), List.of(), false, false,
                        new AmountThisWay(StepKind.FLIP), false, Set.of(), false).ofTriggeringPermanent());
    }
}
