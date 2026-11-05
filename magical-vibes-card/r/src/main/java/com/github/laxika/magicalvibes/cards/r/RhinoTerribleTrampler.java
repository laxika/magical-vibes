package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.DistributeCountersAmongTargetsEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "MSC", collectorNumber = "730")
public class RhinoTerribleTrampler extends Card {

    public RhinoTerribleTrampler() {
        PermanentPredicate artifactOrLand = new PermanentAnyOfPredicate(List.of(
                new PermanentIsArtifactPredicate(),
                new PermanentIsLandPredicate()));
        var destroyTargetPermanent = new DestroyTargetPermanentEffect(false);
        target(new PermanentPredicateTargetFilter(artifactOrLand,
                "Target must be an artifact or land"))
                .bindEffect(destroyTargetPermanent);

        PermanentPredicate otherCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentNotPredicate(new PermanentIsSourcePermanentPredicate())));
        var distributeCountersAmongTargets = DistributeCountersAmongTargetsEffect.chosenAmongTargetCreaturesEtb(
                                CounterType.PLUS_ONE_PLUS_ONE, 3, otherCreature);
        var grantKeyword = new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.TARGETS);
        target(new PermanentPredicateTargetFilter(otherCreature, "Target must be another creature"), 0, 3)
                .bindEffect(distributeCountersAmongTargets)
                .bindEffect(grantKeyword);
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                destroyTargetPermanent,
                distributeCountersAmongTargets,
                grantKeyword);
    }
}
