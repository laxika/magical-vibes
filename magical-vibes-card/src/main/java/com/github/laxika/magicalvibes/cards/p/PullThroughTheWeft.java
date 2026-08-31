package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.ReturnUpToOneOfEachFilterFromGraveyardToDestinationsEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "EOE", collectorNumber = "202")
public class PullThroughTheWeft extends Card {

    public PullThroughTheWeft() {
        CardPredicate nonlandPermanent = new CardAllOfPredicate(List.of(
                new CardIsPermanentPredicate(),
                new CardNotPredicate(new CardTypePredicate(CardType.LAND))));
        CardPredicate land = new CardTypePredicate(CardType.LAND);

        addEffect(EffectSlot.SPELL, new ReturnUpToOneOfEachFilterFromGraveyardToDestinationsEffect(
                List.of(nonlandPermanent, nonlandPermanent, land, land),
                List.of(
                        GraveyardChoiceDestination.HAND,
                        GraveyardChoiceDestination.HAND,
                        GraveyardChoiceDestination.BATTLEFIELD,
                        GraveyardChoiceDestination.BATTLEFIELD),
                List.of(
                        "nonland permanent card",
                        "nonland permanent card",
                        "land card",
                        "land card"),
                List.of(0, 0, 0, 0),
                true,
                true));
    }
}
