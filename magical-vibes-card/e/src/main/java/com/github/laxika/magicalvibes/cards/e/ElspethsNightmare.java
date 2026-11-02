package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.ChooseCardsFromTargetHandEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.GraveyardExileScope;
import com.github.laxika.magicalvibes.model.effect.HandChoiceDestination;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtMostPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "THB", collectorNumber = "91")
public class ElspethsNightmare extends Card {

    public ElspethsNightmare() {
        PermanentPredicate chapterOneFilter = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentPowerAtMostPredicate(2),
                new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate())
        ));
        addTrigger(Triggers.chapter(1), new DestroyTargetPermanentEffect(chapterOneFilter));
        setSagaChapterTargetFilter(1, Set.of(
                new PermanentPredicateTargetFilter(
                        chapterOneFilter,
                        "Target must be a creature an opponent controls with power 2 or less")));
        setSagaChapterTargetRequired(1);

        addTrigger(Triggers.chapter(2), new ChooseCardsFromTargetHandEffect(
                1, List.of(CardType.CREATURE, CardType.LAND), HandChoiceDestination.DISCARD));
        setSagaChapterTargetFilter(2, Set.of(
                new PlayerPredicateTargetFilter(
                        new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                        "Target must be an opponent")));

        addTrigger(Triggers.chapter(3),
                new ExileGraveyardCardsEffect(GraveyardExileScope.TARGET_PLAYER_ENTIRE));
        setSagaChapterTargetFilter(3, Set.of(
                new PlayerPredicateTargetFilter(
                        new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                        "Target must be an opponent")));
    }
}
