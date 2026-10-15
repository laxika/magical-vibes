package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.LicidBecomeAuraEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "TMP", collectorNumber = "91")
public class StingingLicid extends Card {

    public StingingLicid() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}{U}",
                List.of(new LicidBecomeAuraEffect("{U}")),
                "{1}{U}, {T}: This creature loses this ability and becomes an Aura enchantment with enchant"
                        + " creature. Attach it to target creature. You may pay {U} to end this effect.",
                TargetFilters.creature()
        ));
        // Whenever enchanted creature becomes tapped, this creature deals 2 damage to that creature's controller.
        addEffect(EffectSlot.ON_ENCHANTED_PERMANENT_TAPPED,
                new DealDamageToPlayersEffect(2, DamageRecipient.TRIGGERING_PERMANENT_CONTROLLER));
    }
}
