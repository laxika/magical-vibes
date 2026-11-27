package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RequirePaymentToAttackEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.TapMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "HML", collectorNumber = "55")
public class KoskunFalls extends Card {

    public KoskunFalls() {
        // At the beginning of your upkeep, sacrifice this enchantment unless you tap an untapped
        // creature you control (CR 118.12a). Tapping is the whole payment, so there is no "if you do" effect.
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), UnlessPaysEffect.unlessYouPay(
                new TapMultiplePermanentsCost(1, new PermanentIsCreaturePredicate()).withDescription("creature"), new SacrificeSelfEffect())
                .withPrompt("Tap an untapped creature you control?"));

        // Creatures can't attack you unless their controller pays {2} for each creature they
        // control that's attacking you.
        addStaticEffect(RequirePaymentToAttackEffect.playerOnly(2));
    }
}
