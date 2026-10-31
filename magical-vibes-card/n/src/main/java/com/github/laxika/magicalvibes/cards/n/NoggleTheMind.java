package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.BecomeColorlessEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.LosesAllAbilitiesEffect;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "ECL", collectorNumber = "60")
public class NoggleTheMind extends Card {

    public NoggleTheMind() {
        target(TargetFilters.creature())
                .addStaticEffect(new BecomeColorlessEffect(GrantScope.ENCHANTED_CREATURE))
                .addStaticEffect(new GrantSubtypeEffect(CardSubtype.NOGGLE, GrantScope.ENCHANTED_CREATURE, true))
                .addStaticEffect(new LosesAllAbilitiesEffect(GrantScope.ENCHANTED_CREATURE))
                .addStaticEffect(new SetBasePowerToughnessEffect(1, 1, GrantScope.ENCHANTED_CREATURE));
    }
}
