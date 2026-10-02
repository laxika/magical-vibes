package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "ODY", collectorNumber = "287")
@CardRegistration(set = "SLD", collectorNumber = "140")
@CardRegistration(set = "DMR", collectorNumber = "188")
@CardRegistration(set = "OTP", collectorNumber = "41")
@CardRegistration(set = "MKC", collectorNumber = "204")
@CardRegistration(set = "OTC", collectorNumber = "220")
@CardRegistration(set = "FIC", collectorNumber = "323")
@CardRegistration(set = "BLC", collectorNumber = "251")
@CardRegistration(set = "NEC", collectorNumber = "137")
@CardRegistration(set = "C18", collectorNumber = "175")
public class Decimate extends Card {

    public Decimate() {
        target(TargetFilters.artifact())
                .addEffect(EffectSlot.SPELL, new DestroyTargetPermanentEffect());
        target(TargetFilters.creature())
                .addEffect(EffectSlot.SPELL, new DestroyTargetPermanentEffect());
        target(TargetFilters.enchantment())
                .addEffect(EffectSlot.SPELL, new DestroyTargetPermanentEffect());
        target(TargetFilters.land())
                .addEffect(EffectSlot.SPELL, new DestroyTargetPermanentEffect());
        setAllowSharedTargets(true);
    }
}
