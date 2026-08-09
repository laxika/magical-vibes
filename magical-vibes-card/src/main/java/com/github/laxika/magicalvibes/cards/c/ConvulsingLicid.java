package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureCantAttackOrBlockEffect;
import com.github.laxika.magicalvibes.model.effect.LicidBecomeAuraEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "STH", collectorNumber = "77")
public class ConvulsingLicid extends Card {

    public ConvulsingLicid() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{R}",
                List.of(new LicidBecomeAuraEffect("{R}")),
                "{R}, {T}: This creature loses this ability and becomes an Aura enchantment with enchant"
                        + " creature. Attach it to target creature. You may pay {R} to end this effect.",
                TargetFilters.creature()
        ));
        addEffect(EffectSlot.STATIC, new EnchantedCreatureCantAttackOrBlockEffect(false, true));
    }
}
