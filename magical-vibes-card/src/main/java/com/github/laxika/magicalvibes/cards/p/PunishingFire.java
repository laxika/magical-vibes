package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsSelfPredicate;

@CardRegistration(set = "ZEN", collectorNumber = "142")
public class PunishingFire extends Card {

    public PunishingFire() {
        addEffect(EffectSlot.SPELL, new DealDamageToAnyTargetEffect(2));

        addEffect(EffectSlot.GRAVEYARD_ON_OPPONENT_GAINS_LIFE,
                new MayPayManaEffect("{R}",
                        ReturnCardFromGraveyardEffect.builder()
                                .destination(GraveyardChoiceDestination.HAND)
                                .filter(new CardIsSelfPredicate())
                                .returnAll(true)
                                .build(),
                        "Pay {R} to return Punishing Fire from your graveyard to your hand?"));
    }
}
