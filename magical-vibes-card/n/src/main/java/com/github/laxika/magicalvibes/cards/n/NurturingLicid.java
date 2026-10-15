package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.LicidBecomeAuraEffect;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "TMP", collectorNumber = "242")
public class NurturingLicid extends Card {

    public NurturingLicid() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{G}",
                List.of(new LicidBecomeAuraEffect("{G}")),
                "{G}, {T}: This creature loses this ability and becomes an Aura enchantment with enchant"
                        + " creature. Attach it to target creature. You may pay {G} to end this effect.",
                TargetFilters.creature()
        ));
        // Kept while it is an Aura; does nothing while it is still a creature with nothing enchanted.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{G}",
                List.of(RegenerateEffect.enchantedCreature()),
                "{G}: Regenerate enchanted creature."
        ));
    }
}
