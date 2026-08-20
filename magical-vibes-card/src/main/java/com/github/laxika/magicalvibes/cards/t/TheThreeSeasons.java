package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleUpToThreeCardsFromEachGraveyardIntoOwnersLibrariesEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSupertypePredicate;

@CardRegistration(set = "KHM", collectorNumber = "231")
public class TheThreeSeasons extends Card {

    public TheThreeSeasons() {
        addEffect(EffectSlot.SAGA_CHAPTER_I, new MillEffect(3, MillRecipient.CONTROLLER));
        addEffect(EffectSlot.SAGA_CHAPTER_II, ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.HAND)
                .filter(new CardAllOfPredicate(java.util.List.of(
                        new CardIsPermanentPredicate(),
                        new CardSupertypePredicate(CardSupertype.SNOW))))
                .targetGraveyard(true)
                .upTo(true)
                .build());
        addEffect(EffectSlot.SAGA_CHAPTER_III,
                new ShuffleUpToThreeCardsFromEachGraveyardIntoOwnersLibrariesEffect());
    }
}
