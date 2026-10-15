package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DoubleTargetCreaturePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "C18", collectorNumber = "33")
public class NyleasColossus extends Card {

    public NyleasColossus() {
        // Constellation — Whenever this creature or another enchantment you control enters,
        // double target creature's power and toughness until end of turn.
        target(TargetFilters.creature())
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                        new DoubleTargetCreaturePowerToughnessEffect());
        addEffect(EffectSlot.ON_ALLY_ENCHANTMENT_ENTERS_BATTLEFIELD,
                new DoubleTargetCreaturePowerToughnessEffect());
    }
}
