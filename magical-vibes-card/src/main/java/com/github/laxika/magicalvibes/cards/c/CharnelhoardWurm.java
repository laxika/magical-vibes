package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;

@CardRegistration(set = "CON", collectorNumber = "100")
public class CharnelhoardWurm extends Card {

    public CharnelhoardWurm() {
        // Trample is auto-loaded from the Scryfall keyword.
        // "Whenever this creature deals damage to an opponent, you may return target card from your
        // graveyard to your hand." Any damage to a player (ON_DAMAGE_TO_PLAYER, like Witherscale
        // Wurm). The graveyard target is chosen as the trigger goes on the stack; the trigger path
        // allows an empty selection, so "you may return target" reads as up-to-one (decline = 0
        // chosen) with no MayEffect wrapper.
        addEffect(EffectSlot.ON_DAMAGE_TO_PLAYER, ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.HAND)
                .targetGraveyard(true)
                .build());
    }
}
