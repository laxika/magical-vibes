package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;

@CardRegistration(set = "M13", collectorNumber = "14")
public class FaithsReward extends Card {

    public FaithsReward() {
        // Return to the battlefield all permanent cards in your graveyard that were put there
        // from the battlefield this turn.
        addEffect(EffectSlot.SPELL, ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                .filter(new CardIsPermanentPredicate())
                .returnAll(true)
                .fromBattlefieldThisTurn(true)
                .build());
    }
}
