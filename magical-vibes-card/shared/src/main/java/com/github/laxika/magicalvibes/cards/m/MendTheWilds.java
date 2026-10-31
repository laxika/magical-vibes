package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;

public class MendTheWilds extends Card {

    public MendTheWilds() {
        addSpellEffect(ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.TOP_OF_OWNERS_LIBRARY)
                .filter(new CardIsPermanentPredicate())
                .targetGraveyard(true)
                .build());
    }
}
