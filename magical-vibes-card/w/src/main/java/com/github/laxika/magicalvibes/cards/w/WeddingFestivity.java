package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;

/**
 * Wedding Festivity — back face of Wedding Announcement // Wedding Festivity.
 * Enchantment
 * Creatures you control get +1/+1.
 */
public class WeddingFestivity extends Card {

    public WeddingFestivity() {
        // Creatures you control get +1/+1.
        addStaticEffect(new StaticBoostEffect(1, 1, GrantScope.OWN_CREATURES));
    }
}
