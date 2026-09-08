package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * Produces mana that can only be spent under the given {@link ManaRestriction} (e.g. instant/sorcery
 * only, artifact spells only, Myr spells only, kicked spells only). The restriction routes the mana
 * into the matching {@link ManaPool} bucket; the spend side keys on those buckets.
 */
public record AwardRestrictedManaEffect(ManaColor color, DynamicAmount amount, ManaRestriction restriction) implements ManaProducingEffect {

    public AwardRestrictedManaEffect(ManaColor color, int amount, ManaRestriction restriction) {
        this(color, new Fixed(amount), restriction);
    }

    public void applyTo(ManaPool pool, int resolvedAmount) {
        restriction.applyTo(pool, color, resolvedAmount);
    }
}
