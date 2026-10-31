package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureCantAttackOrBlockEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "PLC", collectorNumber = "142")
@CardRegistration(set = "TSR", collectorNumber = "245")
public class UtopiaVow extends Card {

    public UtopiaVow() {
        target(TargetFilters.creature())
                .addStaticEffect(new EnchantedCreatureCantAttackOrBlockEffect())
                .addStaticEffect(new GrantActivatedAbilityEffect(
                        ManaAbilities.tapForAnyColor(),
                        GrantScope.ENCHANTED_CREATURE
                ));
    }
}
