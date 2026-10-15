package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BecomeEnchantmentUntilCreatureSpellCastEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "USG", collectorNumber = "53")
public class SoulSculptor extends Card {

    public SoulSculptor() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}{W}",
                List.of(new BecomeEnchantmentUntilCreatureSpellCastEffect()),
                "{1}{W}, {T}: Target creature becomes an enchantment and loses all abilities until a player casts a creature spell.",
                TargetFilters.creature()
        ));
    }
}
