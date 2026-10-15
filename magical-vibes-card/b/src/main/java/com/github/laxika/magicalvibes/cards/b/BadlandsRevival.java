package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.ReturnUpToOneOfEachFilterFromGraveyardToDestinationsEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "OTJ", collectorNumber = "194")
public class BadlandsRevival extends Card {

    public BadlandsRevival() {
        addEffect(EffectSlot.SPELL, new ReturnUpToOneOfEachFilterFromGraveyardToDestinationsEffect(
                List.of(new CardTypePredicate(CardType.CREATURE), new CardIsPermanentPredicate()),
                List.of(GraveyardChoiceDestination.BATTLEFIELD, GraveyardChoiceDestination.HAND),
                List.of("creature card", "permanent card")));
    }
}
