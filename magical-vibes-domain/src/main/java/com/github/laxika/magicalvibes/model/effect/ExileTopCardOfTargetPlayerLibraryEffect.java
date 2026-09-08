package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * Exiles the top card of the targeted player's library and conditionally applies effects when that
 * card is a land. The card type is checked before the card leaves the library.
 */
public record ExileTopCardOfTargetPlayerLibraryEffect(int lifeGainIfLand, CardEffect effectIfLand)
        implements LifeGainEffect {

    public ExileTopCardOfTargetPlayerLibraryEffect(int lifeGainIfLand) {
        this(lifeGainIfLand, null);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.player());
    }

    @Override
    public DynamicAmount lifeGainAmount() {
        return new Fixed(lifeGainIfLand);
    }
}
