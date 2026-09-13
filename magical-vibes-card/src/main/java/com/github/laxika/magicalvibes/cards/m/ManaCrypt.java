package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.FlipCoinWinEffect;

@CardRegistration(set = "ME2", collectorNumber = "214")
@CardRegistration(set = "VMA", collectorNumber = "272")
@CardRegistration(set = "MPS", collectorNumber = "16")
public class ManaCrypt extends Card {

    public ManaCrypt() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new FlipCoinWinEffect(
                null,
                new DealDamageToPlayersEffect(3, DamageRecipient.CONTROLLER)));
        addEffect(EffectSlot.ON_TAP, new AwardManaEffect(ManaColor.COLORLESS, 2));
    }
}
