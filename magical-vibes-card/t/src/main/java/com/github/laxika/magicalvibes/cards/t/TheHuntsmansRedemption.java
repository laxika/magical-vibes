package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.SagaChapterTargetGroup;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentThenEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "WOE", collectorNumber = "176")
public class TheHuntsmansRedemption extends Card {

    public TheHuntsmansRedemption() {
        addTrigger(Triggers.chapter(1), new CreateTokenEffect(
                "Beast", 3, 3, CardColor.GREEN,
                List.of(CardSubtype.BEAST), Set.of(), Set.of()));

        addTrigger(Triggers.chapter(2), new MayEffect(
                new SacrificePermanentThenEffect(
                        new PermanentIsCreaturePredicate(),
                        new SearchLibraryEffect(
                                new CardAnyOfPredicate(List.of(
                                        new CardTypePredicate(CardType.CREATURE),
                                        CardPredicateUtils.basicLand())),
                                LibrarySearchDestination.HAND),
                        "a creature", false, false),
                "Sacrifice a creature?"));

        addTrigger(Triggers.chapter(3),
                new BoostTargetCreatureEffect(2, 2),
                new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.TARGET));
        setSagaChapterTargetGroups(3, List.of(
                new SagaChapterTargetGroup(TargetFilters.creature(), 0, 2)));
    }
}
