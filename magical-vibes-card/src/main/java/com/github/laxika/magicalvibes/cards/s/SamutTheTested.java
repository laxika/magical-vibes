package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.DealDividedDamageEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "HOU", collectorNumber = "144")
public class SamutTheTested extends Card {

    public SamutTheTested() {
        // +1: Up to one target creature gains double strike until end of turn.
        addActivatedAbility(new ActivatedAbility(
                false, null,
                List.of(new GrantKeywordEffect(Keyword.DOUBLE_STRIKE, GrantScope.TARGET)),
                "+1: Up to one target creature gains double strike until end of turn.",
                new PermanentPredicateTargetFilter(
                        new PermanentIsCreaturePredicate(), "Target must be a creature"),
                +1, null, null,
                List.of(), 0, 1
        ));

        // −2: Samut deals 2 damage divided as you choose among one or two targets.
        addActivatedAbility(new ActivatedAbility(
                -2,
                List.of(DealDividedDamageEffect.chosenAmongAnyTargets(2)),
                "−2: Samut deals 2 damage divided as you choose among one or two targets."
        ));

        // −7: Search your library for up to two creature and/or planeswalker cards, put them onto
        // the battlefield, then shuffle.
        addActivatedAbility(new ActivatedAbility(
                -7,
                List.of(new SearchLibraryEffect(
                        new Fixed(2),
                        new CardAnyOfPredicate(List.of(
                                new CardTypePredicate(CardType.CREATURE),
                                new CardTypePredicate(CardType.PLANESWALKER)
                        )),
                        LibrarySearchDestination.BATTLEFIELD)),
                "−7: Search your library for up to two creature and/or planeswalker cards, "
                        + "put them onto the battlefield, then shuffle."
        ));
    }
}
