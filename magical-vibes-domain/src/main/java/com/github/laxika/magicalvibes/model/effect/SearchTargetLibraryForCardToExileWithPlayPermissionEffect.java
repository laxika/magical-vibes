package com.github.laxika.magicalvibes.model.effect;

/**
 * Searches target opponent's library for any card, exiles it face down,
 * shuffles that player's library, and grants the caster permission to play
 * the exiled card.
 *
 * <p>When {@code expiresAtNextUpkeep} is {@code false} the permission lasts for as long as the card
 * remains exiled (Praetor's Grasp). When {@code true} the permission lasts only until the caster's
 * next upkeep, at which point an unplayed card is put into its owner's graveyard (Grinning Totem).
 */
public record SearchTargetLibraryForCardToExileWithPlayPermissionEffect(boolean expiresAtNextUpkeep) implements CardEffect {

    public SearchTargetLibraryForCardToExileWithPlayPermissionEffect() {
        this(false);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetCategory.PLAYER);
    }
}
