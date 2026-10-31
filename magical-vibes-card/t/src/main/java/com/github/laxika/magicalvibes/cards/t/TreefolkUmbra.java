package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AssignCombatDamageWithToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.TotemArmorEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "MH1", collectorNumber = "185")
public class TreefolkUmbra extends Card {

    public TreefolkUmbra() {
        target(TargetFilters.creature())
                .addStaticEffect(new StaticBoostEffect(0, 2, GrantScope.ENCHANTED_CREATURE))
                .addStaticEffect(new AssignCombatDamageWithToughnessEffect(GrantScope.ENCHANTED_CREATURE, true))
                .addStaticEffect(new TotemArmorEffect());
    }
}
