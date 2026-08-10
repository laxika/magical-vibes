package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * Reveals the top card of the {@link LibraryOwner}'s library. The card stays on top — no draw, no
 * reorder. Pair with a {@code ConditionalEffect} reading the revealed card for an "if it's a land
 * card …" rider (the Deceivers and Paroxysm).
 *
 * <p>{@code lifeGainIfLand} lets the controller gain that much life when the revealed card is a
 * land card (Prophecy); {@code 0} means no life gain (Aven Windreader, the Deceivers). The
 * {@link LifeGainEffect} capability therefore reports {@code Fixed(0)} for every reveal that has no
 * rider, which is a fact and not a score — consumers that ask "is this a life-gain card" must treat
 * a zero amount as "no".
 */
public record RevealTopCardOfLibraryEffect(LibraryOwner owner, int lifeGainIfLand)
        implements LifeGainEffect {

    /** Convenience constructor for a reveal with no life-gain rider. */
    public RevealTopCardOfLibraryEffect(LibraryOwner owner) {
        this(owner, 0);
    }

    @Override
    public TargetSpec targetSpec() {
        return owner == LibraryOwner.TARGET_PLAYER
                ? TargetSpec.benign(TargetPredicates.player())
                : TargetSpec.NONE;
    }

    @Override
    public DynamicAmount lifeGainAmount() {
        return new Fixed(lifeGainIfLand);
    }
}
