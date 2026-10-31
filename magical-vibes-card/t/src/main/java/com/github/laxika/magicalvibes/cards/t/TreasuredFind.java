package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.ExileSpellEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;

@CardRegistration(set = "RTR", collectorNumber = "204")
@CardRegistration(set = "DDM", collectorNumber = "70")
@CardRegistration(set = "GK1", collectorNumber = "72")
public class TreasuredFind extends Card {

    public TreasuredFind() {
        // Return target card from your graveyard to your hand.
        addSpellEffect(ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.HAND)
                        .targetGraveyard(true)
                        .build());

        // Exile Treasured Find.
        addSpellEffect(new ExileSpellEffect());
    }
}
