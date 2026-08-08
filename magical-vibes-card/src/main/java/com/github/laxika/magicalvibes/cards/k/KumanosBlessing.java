package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileCreaturesDamagedBySourceInsteadOfDyingEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "BOK", collectorNumber = "111")
public class KumanosBlessing extends Card {

    public KumanosBlessing() {
        // Flash is driven automatically by the Scryfall-loaded keyword.
        // Enchant creature. If a creature dealt damage by enchanted creature this turn would die,
        // exile it instead — PermanentRemovalService reads this STATIC effect from Auras attached
        // to the damaging permanent (same replacement as Frostwielder / Kumano's Pupils).
        target(TargetFilters.creature())
                .addEffect(EffectSlot.STATIC, new ExileCreaturesDamagedBySourceInsteadOfDyingEffect());
    }
}
