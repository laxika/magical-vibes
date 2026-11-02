package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerChoosesPermanentsThenDestroyRestEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ACR", collectorNumber = "4")
public class FallOfTheFirstCivilization extends Card {

    public FallOfTheFirstCivilization() {
        var opponentFilter = new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent.");
        target(opponentFilter)
                .addTrigger(Triggers.chapter(1), SequenceEffect.of(
                        new DrawCardEffect(2),
                        new DrawCardForTargetPlayerEffect(2, false, true)));
        setSagaChapterTargetFilter(1, Set.of(opponentFilter));

        var opponentArtifact = new PermanentAllOfPredicate(List.of(
                new PermanentIsArtifactPredicate(),
                new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate())));
        var opponentArtifactFilter = new PermanentPredicateTargetFilter(
                opponentArtifact,
                "Target must be an artifact an opponent controls.");
        target(opponentArtifactFilter)
                .addTrigger(Triggers.chapter(2), new ExileTargetPermanentEffect(opponentArtifact));
        setSagaChapterTargetFilter(2, Set.of(opponentArtifactFilter));
        setSagaChapterTargetRequired(2);

        addTrigger(Triggers.chapter(3),
                new EachPlayerChoosesPermanentsThenDestroyRestEffect(
                        3, new PermanentNotPredicate(new PermanentIsLandPredicate())));
    }
}
