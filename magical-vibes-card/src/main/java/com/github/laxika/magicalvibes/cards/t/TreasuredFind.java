package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.ExileSpellEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;

@CardRegistration(set = "RTR", collectorNumber = "204")
public class TreasuredFind extends Card {

    public TreasuredFind() {
        // Return target card from your graveyard to your hand.
        addEffect(EffectSlot.SPELL,
                ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.HAND)
                        .targetGraveyard(true)
                        .build());

        // Exile Treasured Find.
        addEffect(EffectSlot.SPELL, new ExileSpellEffect());
    }
}
