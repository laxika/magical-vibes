package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.amount.CardsInLibrary;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MRD", collectorNumber = "212")
public class MyrIncubator extends Card {

    public MyrIncubator() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{6}",
                List.of(
                        new SacrificeSelfCost(),
                        new SearchLibraryEffect(new CardsInLibrary(CountScope.CONTROLLER),
                                new CardTypePredicate(CardType.ARTIFACT), LibrarySearchDestination.EXILE),
                        new CreateTokenEffect(new AmountThisWay(StepKind.SEARCH), "Myr", 1, 1, null,
                                List.of(CardSubtype.MYR), Set.of(), Set.of(CardType.ARTIFACT))
                ),
                "{6}, {T}, Sacrifice this artifact: Search your library for any number of artifact cards, exile them, then create that many 1/1 colorless Myr artifact creature tokens. Then shuffle."
        ));
    }
}
