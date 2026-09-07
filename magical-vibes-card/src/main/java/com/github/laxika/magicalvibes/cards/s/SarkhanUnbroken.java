package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.amount.CardsInLibrary;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "DTK", collectorNumber = "230")
public class SarkhanUnbroken extends Card {

    public SarkhanUnbroken() {
        addActivatedAbility(new ActivatedAbility(
                +1,
                List.of(new DrawCardEffect(), new AwardAnyColorManaEffect()),
                "+1: Draw a card, then add one mana of any color."
        ));

        addActivatedAbility(new ActivatedAbility(
                -2,
                List.of(new CreateTokenEffect("Dragon", 4, 4, CardColor.RED,
                        List.of(CardSubtype.DRAGON), Set.of(Keyword.FLYING), Set.of())),
                "-2: Create a 4/4 red Dragon creature token with flying."
        ));

        addActivatedAbility(new ActivatedAbility(
                -8,
                List.of(new SearchLibraryEffect(
                        new CardsInLibrary(CountScope.CONTROLLER),
                        new CardAllOfPredicate(List.of(
                                new CardTypePredicate(CardType.CREATURE),
                                new CardSubtypePredicate(CardSubtype.DRAGON))),
                        LibrarySearchDestination.BATTLEFIELD)),
                "-8: Search your library for any number of Dragon creature cards, put them onto the battlefield, then shuffle."
        ));
    }
}
