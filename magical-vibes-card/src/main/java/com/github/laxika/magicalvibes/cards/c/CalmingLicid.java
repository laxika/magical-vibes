package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureCantAttackOrBlockEffect;
import com.github.laxika.magicalvibes.model.effect.LicidBecomeAuraEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "STH", collectorNumber = "2")
public class CalmingLicid extends Card {

    public CalmingLicid() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{W}",
                List.of(new LicidBecomeAuraEffect("{W}")),
                "{W}, {T}: This creature loses this ability and becomes an Aura enchantment with enchant"
                        + " creature. Attach it to target creature. You may pay {W} to end this effect.",
                TargetFilters.creature()
        ));
        addEffect(EffectSlot.STATIC, new EnchantedCreatureCantAttackOrBlockEffect(true, false));
    }
}
