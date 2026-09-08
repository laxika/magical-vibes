package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.amount.CardsInLibrary;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNamedPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "ZEN", collectorNumber = "170")
public class NissaRevane extends Card {

    public NissaRevane() {
        addActivatedAbility(new ActivatedAbility(
                +1,
                List.of(new SearchLibraryEffect(
                        new CardNamedPredicate("Nissa's Chosen"),
                        LibrarySearchDestination.BATTLEFIELD)),
                "+1: Search your library for a card named Nissa's Chosen, put it onto the battlefield, then shuffle."
        ));

        addActivatedAbility(new ActivatedAbility(
                +1,
                List.of(new GainLifeEffect(new Scaled(
                        new PermanentCount(new PermanentHasSubtypePredicate(CardSubtype.ELF), CountScope.CONTROLLER),
                        2))),
                "+1: You gain 2 life for each Elf you control."
        ));

        addActivatedAbility(new ActivatedAbility(
                -7,
                List.of(new SearchLibraryEffect(
                        new CardsInLibrary(CountScope.CONTROLLER),
                        new CardAllOfPredicate(List.of(
                                new CardTypePredicate(CardType.CREATURE),
                                new CardSubtypePredicate(CardSubtype.ELF))),
                        LibrarySearchDestination.BATTLEFIELD)),
                "-7: Search your library for any number of Elf creature cards, put them onto the battlefield, then shuffle."
        ));
    }
}
