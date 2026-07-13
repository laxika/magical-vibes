package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardNamedPredicate;

@CardRegistration(set = "SHM", collectorNumber = "183")
public class DemigodOfRevenge extends Card {

    public DemigodOfRevenge() {
        // When you cast this spell, return all cards named Demigod of Revenge from your graveyard to
        // the battlefield. (Flying and haste are auto-loaded keywords.)
        addEffect(EffectSlot.ON_SELF_CAST, ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                .filter(new CardNamedPredicate("Demigod of Revenge"))
                .returnAll(true)
                .build());
    }
}
