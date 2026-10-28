package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
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
@CardRegistration(set = "C16", collectorNumber = "193")
public class Decimate extends Card {

    public Decimate() {
        // One "destroy" instruction over four targets (CR 608.2c: read the whole text), so one effect
        // instance is bound to every target group and all four are destroyed at the same time.
        CardEffect destroy = new DestroyTargetPermanentEffect();
        target(TargetFilters.artifact())
                .addEffect(EffectSlot.SPELL, destroy);
        target(TargetFilters.creature())
                .bindEffect(destroy);
        target(TargetFilters.enchantment())
                .bindEffect(destroy);
        target(TargetFilters.land())
                .bindEffect(destroy);
        setAllowSharedTargets(true);
    }
}
