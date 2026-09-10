package com.github.laxika.magicalvibes.model.effect;

/**
 * Grants the controller permission to look at and play the top card of the target player's
 * library until end of turn, including permission to spend mana as though it were mana of any
 * color to cast spells played this way.
 */
public record GrantControllerLibraryTopPermissionForTargetPlayerUntilEndOfTurnEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.player());
    }
}
