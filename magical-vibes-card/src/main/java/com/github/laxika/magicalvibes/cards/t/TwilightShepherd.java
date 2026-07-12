package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;

@CardRegistration(set = "SHM", collectorNumber = "25")
public class TwilightShepherd extends Card {

    public TwilightShepherd() {
        // Flying, vigilance and persist are loaded from Scryfall and resolved automatically.

        // When this creature enters, return to your hand all cards in your graveyard that were put
        // there from the battlefield this turn.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.HAND)
                .returnAll(true)
                .fromBattlefieldThisTurn(true)
                .build());
    }
}
