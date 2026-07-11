package com.github.laxika.magicalvibes.model.effect;

/**
 * ON_ALLY_CREATURE_ENTERS_BATTLEFIELD value-materialising marker: "you may look at the top X cards
 * of your library, where X is that creature's power. If you do, put one of those cards on top of
 * your library and the rest on the bottom of your library in any order." The entering creature's
 * power is read at trigger time and baked into a concrete
 * {@link LookAtTopCardsPutOneOnTopRestOnBottomEffect} wrapped in a {@link MayEffect} (mirroring
 * {@link PutCountersOnSourceEqualToEnteringPowerEffect}). Used by Cream of the Crop.
 */
public record LookAtTopCardsEqualToEnteringPowerPutOneOnTopRestOnBottomEffect() implements CardEffect {
}
