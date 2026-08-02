package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayPayTapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.RequirePaymentToAttackEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.TapMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "HML", collectorNumber = "55")
public class KoskunFalls extends Card {

    public KoskunFalls() {
        // At the beginning of your upkeep, sacrifice this enchantment unless you tap an untapped
        // creature you control. Tapping is the whole payment, so there is no "if you do" effect.
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new MayPayTapPermanentsEffect(
                new TapMultiplePermanentsCost(1, new PermanentIsCreaturePredicate()),
                null,
                "Tap an untapped creature you control?",
                new SacrificeSelfEffect()));

        // Creatures can't attack you unless their controller pays {2} for each creature they
        // control that's attacking you.
        addEffect(EffectSlot.STATIC, RequirePaymentToAttackEffect.playerOnly(2));
    }
}
