package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureCantAttackOrBlockEffect;
import com.github.laxika.magicalvibes.model.effect.ExileEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ARB", collectorNumber = "144")
public class Crystallization extends Card {

    public Crystallization() {
        target(TargetFilters.creature()).addStaticEffect(new EnchantedCreatureCantAttackOrBlockEffect())
          .addTrigger(Triggers.becomesTargetOfSpellOrAbility(), new ExileEnchantedCreatureEffect());
    }
}
