package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.effect.ManaValueBound;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

import java.util.List;

@CardRegistration(set = "NEM", collectorNumber = "12")
public class LinSivviDefiantHero extends Card {

    public LinSivviDefiantHero() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{X}",
                List.of(new SearchLibraryEffect(
                        new CardAllOfPredicate(List.of(
                                new CardSubtypePredicate(CardSubtype.REBEL),
                                new CardIsPermanentPredicate())),
                        LibrarySearchDestination.BATTLEFIELD,
                        new ManaValueBound(false, 0))),
                "{X}, {T}: Search your library for a Rebel permanent card with mana value X or less, put it onto the battlefield, then shuffle."
        ));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}",
                List.of(ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.BOTTOM_OF_OWNERS_LIBRARY)
                        .filter(new CardSubtypePredicate(CardSubtype.REBEL))
                        .targetGraveyard(true)
                        .build()),
                "{3}: Put target Rebel card from your graveyard on the bottom of your library."
        ));
    }
}
