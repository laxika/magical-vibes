package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.MultiTargetConstraint;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.PlayersInGame;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyEachTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEachControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MSC", collectorNumber = "41")
public class AgeOfUltron extends Card {

    public AgeOfUltron() {
        PermanentPredicateTargetFilter nonartifactCreatureOpponentControls = new PermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentNotPredicate(new PermanentIsArtifactPredicate()),
                        new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate()))),
                "Target must be a nonartifact creature an opponent controls");
        target(nonartifactCreatureOpponentControls, 0, 99)
                .addTrigger(Triggers.chapter(1), new DestroyEachTargetPermanentEffect());
        setSagaChapterTargetFilter(1, Set.of(nonartifactCreatureOpponentControls));
        setMultiTargetConstraint(MultiTargetConstraint.AT_MOST_ONE_PER_CONTROLLER);

        addTrigger(Triggers.chapter(2), new CreateTokenEffect(
                new Sum(new PlayersInGame(), new Fixed(-1)), "Robot", 2, 2, null,
                List.of(CardSubtype.ROBOT, CardSubtype.VILLAIN), Set.of(), Set.of(CardType.ARTIFACT)));

        PermanentAllOfPredicate artifactCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsArtifactPredicate(), new PermanentIsCreaturePredicate()));
        addTrigger(Triggers.chapter(3), new PutCounterOnEachControlledPermanentEffect(
                CounterType.PLUS_ONE_PLUS_ONE, 1, artifactCreature));
        addTrigger(Triggers.chapter(3), new GrantKeywordEffect(
                Keyword.DEATHTOUCH, GrantScope.ALL_OWN_CREATURES, artifactCreature));
    }
}
