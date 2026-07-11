package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;

/**
 * Produces mana that can only be spent under the given {@link ManaRestriction} (e.g. instant/sorcery
 * only, artifact spells only, Myr spells only, kicked spells only). The restriction routes the mana
 * into the matching {@link ManaPool} bucket; the spend side keys on those buckets.
 */
public record AwardRestrictedManaEffect(ManaColor color, int amount, ManaRestriction restriction) implements ManaProducingEffect {

    public void applyTo(ManaPool pool) {
        restriction.applyTo(pool, color, amount);
    }
}
