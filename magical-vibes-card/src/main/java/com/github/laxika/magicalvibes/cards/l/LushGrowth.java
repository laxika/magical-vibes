package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EnchantedPermanentBecomesTypeEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

/**
 * Lush Growth — Enchant land. Enchanted land is a Mountain, Forest, and Plains.
 *
 * <p>Type-replacing per CR 305.7: the land loses its other land types and printed abilities,
 * and taps for {R}, {G}, or {W}.
 */
@CardRegistration(set = "ALA", collectorNumber = "136")
public class LushGrowth extends Card {

    public LushGrowth() {
        target(TargetFilters.land()).addEffect(EffectSlot.STATIC, new EnchantedPermanentBecomesTypeEffect(
                List.of(CardSubtype.MOUNTAIN, CardSubtype.FOREST, CardSubtype.PLAINS)));
    }
}
