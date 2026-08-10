package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantCardTypeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.LicidBecomeAuraEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "EXO", collectorNumber = "141")
public class TransmogrifyingLicid extends Card {

    public TransmogrifyingLicid() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(new LicidBecomeAuraEffect("{1}")),
                "{1}, {T}: This creature loses this ability and becomes an Aura enchantment with enchant"
                        + " creature. Attach it to target creature. You may pay {1} to end this effect.",
                TargetFilters.creature()
        ));
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(1, 1, GrantScope.ENCHANTED_CREATURE));
        addEffect(EffectSlot.STATIC, new GrantCardTypeEffect(CardType.ARTIFACT, GrantScope.ENCHANTED_CREATURE));
    }
}
