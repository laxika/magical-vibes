package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.a.ArchitectOfRestoration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.DiscardCardThenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfAndReturnTransformedEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.CardMaxManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardSupertypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "NEO", collectorNumber = "34")
public class TheRestorationOfEiganjo extends Card {

    public TheRestorationOfEiganjo() {
        setBackFaceCard(new ArchitectOfRestoration());

        addTrigger(Triggers.chapter(1), new SearchLibraryEffect(new CardAllOfPredicate(List.of(
                new CardSupertypePredicate(CardSupertype.BASIC),
                new CardSubtypePredicate(CardSubtype.PLAINS)))));

        addTrigger(Triggers.chapter(2), new MayEffect(
                new DiscardCardThenEffect(
                        null,
                        ReturnCardFromGraveyardEffect.builder()
                                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                                .filter(new CardAllOfPredicate(List.of(
                                        new CardIsPermanentPredicate(),
                                        new CardMaxManaValuePredicate(2))))
                                .targetGraveyard(true)
                                .enterTapped(true)
                                .build(),
                        "a card"),
                "Discard a card to return target permanent card with mana value 2 or less from your graveyard to the battlefield tapped?"));

        addTrigger(Triggers.chapter(3), new ExileSelfAndReturnTransformedEffect(true));
    }

    @Override
    public String getBackFaceClassName() {
        return "ArchitectOfRestoration";
    }
}
