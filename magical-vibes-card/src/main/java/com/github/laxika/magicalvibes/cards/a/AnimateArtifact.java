package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EnchantedPermanentBecomesCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "4ED", collectorNumber = "60")
public class AnimateArtifact extends Card {

    public AnimateArtifact() {
        // Enchant artifact. As long as enchanted artifact isn't a creature, it's an artifact
        // creature with power and toughness each equal to its mana value.
        target(TargetFilters.artifact()).addEffect(EffectSlot.STATIC, new EnchantedPermanentBecomesCreatureEffect(
                0, 0, null, List.of(), true));
    }
}
