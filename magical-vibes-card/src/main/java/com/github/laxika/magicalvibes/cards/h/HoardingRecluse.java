package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsSelfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;

@CardRegistration(set = "BRO", collectorNumber = "186")
public class HoardingRecluse extends Card {

    public HoardingRecluse() {
        // Reach and deathtouch are auto-loaded from Scryfall.
        // When this creature dies, put up to one other target card from a graveyard on the bottom
        // of its owner's library.
        addEffect(EffectSlot.ON_DEATH, ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.BOTTOM_OF_OWNERS_LIBRARY)
                .source(GraveyardSearchScope.ALL_GRAVEYARDS)
                .filter(new CardNotPredicate(new CardIsSelfPredicate()))
                .targetGraveyard(true)
                .upTo(true)
                .build());
    }
}
