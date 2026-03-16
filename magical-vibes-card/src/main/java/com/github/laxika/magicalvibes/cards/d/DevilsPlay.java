package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.effect.DealXDamageToAnyTargetEffect;

@CardRegistration(set = "ISD", collectorNumber = "140")
public class DevilsPlay extends Card {

    public DevilsPlay() {
        addEffect(EffectSlot.SPELL, new DealXDamageToAnyTargetEffect());
        addCastingOption(new FlashbackCast("{X}{R}{R}{R}"));
    }
}
