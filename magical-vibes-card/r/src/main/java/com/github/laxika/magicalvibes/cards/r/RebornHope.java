package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsMulticoloredPredicate;

@CardRegistration(set = "ARB", collectorNumber = "76")
public class RebornHope extends Card {

    public RebornHope() {
        // Return target multicolored card from your graveyard to your hand.
        addSpellEffect(ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.HAND)
                .filter(new CardIsMulticoloredPredicate())
                .targetGraveyard(true)
                .build());
    }
}
