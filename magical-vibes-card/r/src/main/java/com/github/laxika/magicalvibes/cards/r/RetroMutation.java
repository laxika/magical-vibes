package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureCantAttackOrBlockEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.LosesAllAbilitiesEffect;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "TMT", collectorNumber = "51")
public class RetroMutation extends Card {

    public RetroMutation() {
        target(TargetFilters.creature())
                .addStaticEffect(new GrantSubtypeEffect(CardSubtype.TURTLE, GrantScope.ENCHANTED_CREATURE, true))
                .addStaticEffect(new SetBasePowerToughnessEffect(0, 1, GrantScope.ENCHANTED_CREATURE))
                .addStaticEffect(new LosesAllAbilitiesEffect(GrantScope.ENCHANTED_CREATURE))
                .addStaticEffect(new EnchantedCreatureCantAttackOrBlockEffect(true, false));
    }
}
