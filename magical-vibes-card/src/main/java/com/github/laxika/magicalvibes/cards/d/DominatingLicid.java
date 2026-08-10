package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ControlEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.LicidBecomeAuraEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "EXO", collectorNumber = "30")
public class DominatingLicid extends Card {

    public DominatingLicid() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}{U}{U}",
                List.of(new LicidBecomeAuraEffect("{U}")),
                "{1}{U}{U}, {T}: This creature loses this ability and becomes an Aura enchantment with enchant"
                        + " creature. Attach it to target creature. You may pay {U} to end this effect.",
                TargetFilters.creature()
        ));
        addEffect(EffectSlot.STATIC, new ControlEnchantedCreatureEffect());
    }
}
