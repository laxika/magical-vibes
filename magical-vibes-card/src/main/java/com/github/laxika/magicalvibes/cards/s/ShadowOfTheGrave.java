package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;

@CardRegistration(set = "AKH", collectorNumber = "107")
public class ShadowOfTheGrave extends Card {

    public ShadowOfTheGrave() {
        // Return to your hand all cards in your graveyard that you cycled or discarded this turn.
        addEffect(EffectSlot.SPELL, ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.HAND)
                .returnAll(true)
                .discardedOrCycledThisTurn(true)
                .build());
    }
}
