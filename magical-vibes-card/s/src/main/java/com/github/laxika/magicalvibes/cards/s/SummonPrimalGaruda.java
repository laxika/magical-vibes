package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTappedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "FIN", collectorNumber = "37")
@CardRegistration(set = "FIN", collectorNumber = "360")
public class SummonPrimalGaruda extends Card {

    public SummonPrimalGaruda() {
        var tappedCreatureOpponentControls = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentIsTappedPredicate(),
                new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate())
        ));
        var anotherCreatureYouControl = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentControlledBySourceControllerPredicate(),
                new PermanentNotPredicate(new PermanentIsSourceCardPredicate())
        ));

        addTrigger(Triggers.chapter(1),
                new DealDamageToTargetCreatureEffect(4, tappedCreatureOpponentControls));
        setSagaChapterTargetFilter(1, Set.of(
                new PermanentPredicateTargetFilter(
                        tappedCreatureOpponentControls,
                        "Target must be a tapped creature an opponent controls")
        ));
        setSagaChapterTargetRequired(1);

        addTrigger(Triggers.chapter(2),
                new BoostTargetCreatureEffect(1, 0, anotherCreatureYouControl),
                new GrantKeywordEffect(Keyword.FLYING, GrantScope.TARGET, anotherCreatureYouControl));
        setSagaChapterTargetFilter(2, Set.of(
                new PermanentPredicateTargetFilter(
                        anotherCreatureYouControl,
                        "Target must be another creature you control")
        ));
        setSagaChapterTargetRequired(2);

        addTrigger(Triggers.chapter(3),
                new BoostTargetCreatureEffect(1, 0, anotherCreatureYouControl),
                new GrantKeywordEffect(Keyword.FLYING, GrantScope.TARGET, anotherCreatureYouControl));
        setSagaChapterTargetFilter(3, Set.of(
                new PermanentPredicateTargetFilter(
                        anotherCreatureYouControl,
                        "Target must be another creature you control")
        ));
        setSagaChapterTargetRequired(3);
    }
}
