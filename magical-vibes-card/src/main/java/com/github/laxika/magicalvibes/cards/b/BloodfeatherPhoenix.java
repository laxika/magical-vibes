package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsSelfPredicate;

@CardRegistration(set = "MOM", collectorNumber = "132")
public class BloodfeatherPhoenix extends Card {

    public BloodfeatherPhoenix() {
        addEffect(EffectSlot.GRAVEYARD_ON_ALLY_INSTANT_OR_SORCERY_DEALS_DAMAGE_TO_OPPONENT_OR_BATTLE,
                new MayPayManaEffect("{R}",
                        ReturnCardFromGraveyardEffect.builder()
                                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                                .filter(new CardIsSelfPredicate())
                                .returnAll(true)
                                .grantHaste(true)
                                .build(),
                        "Pay {R} to return Bloodfeather Phoenix from your graveyard to the battlefield?"));
    }
}
