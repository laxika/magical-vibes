package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsSelfPredicate;

import java.util.List;

@CardRegistration(set = "XLN", collectorNumber = "156")
public class RepeatingBarrage extends Card {

    public RepeatingBarrage() {
        // Repeating Barrage deals 3 damage to any target.
        addEffect(EffectSlot.SPELL, new DealDamageToAnyTargetEffect(3));

        // Raid — {3}{R}{R}: Return Repeating Barrage from your graveyard to your hand.
        // Activate only if you attacked this turn.
        addGraveyardActivatedAbility(new ActivatedAbility(
                false, "{3}{R}{R}",
                List.of(ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.HAND)
                        .filter(new CardIsSelfPredicate())
                        .returnAll(true)
                        .build()),
                "Raid — {3}{R}{R}: Return Repeating Barrage from your graveyard to your hand. Activate only if you attacked this turn.",
                ActivationTimingRestriction.RAID
        ));
    }
}
