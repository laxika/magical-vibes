package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.LockTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.LosesAllAbilitiesEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPredicates;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "ELD", collectorNumber = "133")
@CardRegistration(set = "ELD", collectorNumber = "364")
@CardRegistration(set = "TDC", collectorNumber = "228")
@CardRegistration(set = "AFC", collectorNumber = "134")
public class OpportunisticDragon extends Card {

    public OpportunisticDragon() {
        target(new PermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate()),
                        new PermanentAnyOfPredicate(List.of(
                                new PermanentHasSubtypePredicate(CardSubtype.HUMAN),
                                new PermanentIsArtifactPredicate())
                        )
                )),
                "Target must be a Human or artifact an opponent controls"
        ))
                .addTrigger(Triggers.enters(ObjectRelation.THIS),
                        new GainControlOfTargetEffect(ControlDuration.WHILE_SOURCE_REMAINS))
                .addTrigger(Triggers.enters(ObjectRelation.THIS),
                        new LosesAllAbilitiesEffect(GrantScope.TARGET, EffectDuration.WHILE_SOURCE_REMAINS))
                .addTrigger(Triggers.enters(ObjectRelation.THIS),
                        new LockTargetPermanentEffect(true, true, false,
                                EffectDuration.WHILE_SOURCE_REMAINS, TargetPredicates.permanent()));
    }
}
