package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.QueueReflexiveAbilityEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.SourceFightsTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledByDefendingPlayerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "WHO", collectorNumber = "98")
@CardRegistration(set = "WHO", collectorNumber = "984")
@CardRegistration(set = "WHO", collectorNumber = "393")
@CardRegistration(set = "WHO", collectorNumber = "703")
public class AceFearlessRebel extends Card {

    public AceFearlessRebel() {
        target(new PermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentControlledByDefendingPlayerPredicate())),
                "Target must be a creature defending player controls"), 0, 1)
                .addTrigger(Triggers.attacks(), new MayEffect(
                        SequenceEffect.of(
                                new SacrificePermanentsEffect(1, new PermanentIsArtifactPredicate(), Subjects.you()),
                                new ConditionalEffect(HappenedThisWay.sacrificed(), new QueueReflexiveAbilityEffect(
                                        SequenceEffect.of(
                                        new PutCountersOnSourceEffect(1, 1, 1),
                                        new SourceFightsTargetCreatureEffect())))),
                        "Sacrifice an artifact?"));
    }
}
