package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsSelfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

import java.util.List;

@CardRegistration(set = "STX", collectorNumber = "22")
public class PilgrimOfTheAges extends Card {

    public PilgrimOfTheAges() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new MayEffect(
                new SearchLibraryEffect(
                        new CardAllOfPredicate(List.of(
                                CardPredicateUtils.basicLand(),
                                new CardSubtypePredicate(CardSubtype.PLAINS))),
                        LibrarySearchDestination.HAND),
                "Search your library for a basic Plains card?"));

        addGraveyardActivatedAbility(new ActivatedAbility(
                false,
                "{6}",
                List.of(ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.HAND)
                        .filter(new CardIsSelfPredicate())
                        .returnAll(true)
                        .build()),
                "{6}: Return this card from your graveyard to your hand."
        ));
    }
}
