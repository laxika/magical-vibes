package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.GrantColorEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.LosesAllAbilitiesEffect;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.SetCardTypesEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.Set;

@CardRegistration(set = "ELD", collectorNumber = "47")
@CardRegistration(set = "2XM", collectorNumber = "52")
public class Frogify extends Card {

    public Frogify() {
        target(TargetFilters.creature())
                .addStaticEffect(new SetCardTypesEffect(Set.of(CardType.CREATURE), GrantScope.ENCHANTED_CREATURE))
                .addStaticEffect(new GrantSubtypeEffect(CardSubtype.FROG, GrantScope.ENCHANTED_CREATURE, true))
                .addStaticEffect(new GrantColorEffect(CardColor.BLUE, GrantScope.ENCHANTED_CREATURE, true))
                .addStaticEffect(new LosesAllAbilitiesEffect(GrantScope.ENCHANTED_CREATURE))
                .addStaticEffect(new SetBasePowerToughnessEffect(1, 1, GrantScope.ENCHANTED_CREATURE));
    }
}
