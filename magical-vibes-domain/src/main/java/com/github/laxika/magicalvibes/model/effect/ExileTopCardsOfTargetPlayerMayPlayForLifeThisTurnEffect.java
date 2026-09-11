package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * Exiles the top cards of the targeted player's library and gives the controller permission to
 * play them until end of turn, paying life equal to a spell's mana value instead of its mana cost.
 */
public record ExileTopCardsOfTargetPlayerMayPlayForLifeThisTurnEffect(DynamicAmount count)
        implements CardEffect {

    public ExileTopCardsOfTargetPlayerMayPlayForLifeThisTurnEffect(int count) {
        this(new Fixed(count));
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.player());
    }
}
