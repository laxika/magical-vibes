package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "M14", collectorNumber = "59")
public class IllusionaryArmor extends Card {

    public IllusionaryArmor() {
        target(TargetFilters.creature())
                // Enchanted creature gets +4/+4.
                .addEffect(EffectSlot.STATIC, new StaticBoostEffect(4, 4, GrantScope.ENCHANTED_CREATURE))
                // When enchanted creature becomes the target of a spell or ability, sacrifice this Aura.
                .addEffect(EffectSlot.ON_BECOMES_TARGET_OF_SPELL_OR_ABILITY, new SacrificeSelfEffect());
    }
}
