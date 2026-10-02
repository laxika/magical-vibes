package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.TargetManaValue;
import com.github.laxika.magicalvibes.model.effect.TargetPermanentControllerMaySacrificeOrDamageEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "DSC", collectorNumber = "164")
@CardRegistration(set = "C18", collectorNumber = "21")
public class EnchantersBane extends Card {

    public EnchantersBane() {
        // At the beginning of your end step, target enchantment deals damage equal to its mana
        // value to its controller unless that player sacrifices it.
        target(TargetFilters.enchantment()).addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED,
                TargetPermanentControllerMaySacrificeOrDamageEffect.withTargetAsDamageSource(
                        new TargetManaValue()));
    }
}
