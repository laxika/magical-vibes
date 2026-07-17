package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.condition.SourceIsTapped;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DoesntUntapEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;

@CardRegistration(set = "5ED", collectorNumber = "388")
public class ManaVault extends Card {

    public ManaVault() {
        // This artifact doesn't untap during your untap step.
        addEffect(EffectSlot.STATIC, DoesntUntapEffect.self());

        // At the beginning of your upkeep, you may pay {4}. If you do, untap this artifact.
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new MayPayManaEffect(
                "{4}",
                new UntapPermanentsEffect(TapUntapScope.SELF),
                "Pay {4} to untap Mana Vault?"));

        // At the beginning of your draw step, if this artifact is tapped, it deals 1 damage to you.
        addEffect(EffectSlot.DRAW_TRIGGERED, new ConditionalEffect(
                new SourceIsTapped(),
                new DealDamageToPlayersEffect(1, DamageRecipient.CONTROLLER)));

        // {T}: Add {C}{C}{C}.
        addEffect(EffectSlot.ON_TAP, new AwardManaEffect(ManaColor.COLORLESS, 3));
    }
}
