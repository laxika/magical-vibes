package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * Exiles the top card of the targeted player's library and conditionally gains life when that
 * card is a land. The card type is checked before the card leaves the library.
 */
public record ExileTopCardOfTargetPlayerLibraryEffect(int lifeGainIfLand) implements LifeGainEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.player());
    }

    @Override
    public DynamicAmount lifeGainAmount() {
        return new Fixed(lifeGainIfLand);
    }
}
