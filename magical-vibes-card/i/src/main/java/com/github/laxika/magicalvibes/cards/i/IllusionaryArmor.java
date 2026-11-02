package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "M14", collectorNumber = "59")
public class IllusionaryArmor extends Card {

    public IllusionaryArmor() {
        target(TargetFilters.creature())
                // Enchanted creature gets +4/+4.
                .addStaticEffect(new StaticBoostEffect(4, 4, GrantScope.ENCHANTED_CREATURE))
                // When enchanted creature becomes the target of a spell or ability, sacrifice this Aura.
                .addTrigger(Triggers.becomesTargetOfSpellOrAbility(), new SacrificeSelfEffect());
    }
}
