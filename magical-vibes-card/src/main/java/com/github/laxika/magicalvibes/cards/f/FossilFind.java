package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;

@CardRegistration(set = "SHM", collectorNumber = "206")
public class FossilFind extends Card {

    public FossilFind() {
        // Return a card at random from your graveyard to your hand.
        // (Reordering the graveyard afterward is purely cosmetic and has no engine effect.)
        addEffect(EffectSlot.SPELL, ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.HAND)
                .returnAtRandom(true)
                .build());
    }
}
