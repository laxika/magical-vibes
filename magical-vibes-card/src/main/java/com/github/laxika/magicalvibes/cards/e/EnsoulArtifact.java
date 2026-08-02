package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EnchantedPermanentBecomesCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "M15", collectorNumber = "54")
public class EnsoulArtifact extends Card {

    public EnsoulArtifact() {
        // Enchant artifact. Enchanted artifact is a 5/5 creature in addition to its other types.
        target(TargetFilters.artifact()).addEffect(EffectSlot.STATIC,
                new EnchantedPermanentBecomesCreatureEffect(5, 5, null, List.of()));
    }
}
