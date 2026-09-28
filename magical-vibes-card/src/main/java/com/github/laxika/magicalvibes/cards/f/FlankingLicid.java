package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.LicidBecomeAuraEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "MB2", collectorNumber = "326")
@CardRegistration(set = "MB2", collectorNumber = "562")
public class FlankingLicid extends Card {

    public FlankingLicid() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{R}",
                List.of(new LicidBecomeAuraEffect("{R}")),
                "{R}, {T}: This creature loses this ability and becomes an Aura enchantment with enchant"
                        + " creature. Attach it to target creature. You may pay {R} to end this effect.",
                TargetFilters.creature()
        ));
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(Keyword.FLANKING, GrantScope.ENCHANTED_CREATURE));
    }
}
