package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.GrantColorEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.LosesAllAbilitiesEffect;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "THB", collectorNumber = "51")
public class Ichthyomorphosis extends Card {

    public Ichthyomorphosis() {
        target(TargetFilters.creature())
                .addStaticEffect(new GrantColorEffect(CardColor.BLUE, GrantScope.ENCHANTED_CREATURE, true))
                .addStaticEffect(new GrantSubtypeEffect(CardSubtype.FISH, GrantScope.ENCHANTED_CREATURE, true))
                .addStaticEffect(new LosesAllAbilitiesEffect(GrantScope.ENCHANTED_CREATURE))
                .addStaticEffect(new SetBasePowerToughnessEffect(0, 1, GrantScope.ENCHANTED_CREATURE));
    }
}
