package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "SHM", collectorNumber = "64")
public class DisturbingPlot extends Card {

    public DisturbingPlot() {
        // Return target creature card from a graveyard to its owner's hand.
        // (Conspire is driven by the Scryfall-loaded CONSPIRE keyword and handled by the casting flow.)
        addEffect(EffectSlot.SPELL, ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.HAND)
                .source(GraveyardSearchScope.ALL_GRAVEYARDS)
                .filter(new CardTypePredicate(CardType.CREATURE))
                .targetGraveyard(true)
                .build());
    }
}
