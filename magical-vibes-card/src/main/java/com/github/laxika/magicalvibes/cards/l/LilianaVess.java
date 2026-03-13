package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForCardToTopOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerDiscardsEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "M10", collectorNumber = "102")
@CardRegistration(set = "M11", collectorNumber = "102")
public class LilianaVess extends Card {

    public LilianaVess() {
        // +1: Target player discards a card.
        addActivatedAbility(new ActivatedAbility(
                +1,
                List.of(new TargetPlayerDiscardsEffect(1)),
                "+1: Target player discards a card."
        ));

        // −2: Search your library for a card, then shuffle and put that card on top.
        addActivatedAbility(new ActivatedAbility(
                -2,
                List.of(new SearchLibraryForCardToTopOfLibraryEffect()),
                "\u22122: Search your library for a card, then shuffle and put that card on top."
        ));

        // −8: Put all creature cards from all graveyards onto the battlefield under your control.
        addActivatedAbility(new ActivatedAbility(
                -8,
                List.of(new ReturnCardFromGraveyardEffect(
                        GraveyardChoiceDestination.BATTLEFIELD,
                        new CardTypePredicate(CardType.CREATURE),
                        GraveyardSearchScope.ALL_GRAVEYARDS,
                        false,
                        true,
                        false,
                        null,
                        false
                )),
                "\u22128: Put all creature cards from all graveyards onto the battlefield under your control."
        ));
    }
}
