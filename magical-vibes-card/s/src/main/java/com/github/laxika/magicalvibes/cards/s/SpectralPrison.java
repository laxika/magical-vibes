package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DoesntUntapEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "AVR", collectorNumber = "75")
public class SpectralPrison extends Card {

    public SpectralPrison() {
        target(TargetFilters.creature())
                // Enchanted creature doesn't untap during its controller's untap step.
                .addEffect(EffectSlot.STATIC, DoesntUntapEffect.enchanted())
                // When enchanted creature becomes the target of a spell, sacrifice this Aura.
                .addEffect(EffectSlot.ON_BECOMES_TARGET_OF_SPELL, new SacrificeSelfEffect());
    }
}
