package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * Reveals the top card of target player's library. The card stays on top.
 *
 * <p>{@code lifeGainIfLand} lets the controller gain that much life when the revealed card is a
 * land card (Prophecy); {@code 0} means no life gain (Aven Windreader).
 */
public record RevealTopCardOfLibraryEffect(int lifeGainIfLand) implements LifeGainEffect {

    public RevealTopCardOfLibraryEffect() {
        this(0);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetCategory.PLAYER);
    }

    @Override
    public DynamicAmount lifeGainAmount() {
        return new Fixed(lifeGainIfLand);
    }
}
