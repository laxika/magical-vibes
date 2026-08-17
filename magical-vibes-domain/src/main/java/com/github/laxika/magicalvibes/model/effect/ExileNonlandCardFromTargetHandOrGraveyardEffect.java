package com.github.laxika.magicalvibes.model.effect;

/**
 * Reveals the target player's hand, then lets the controller choose a nonland card from that
 * hand or that player's graveyard and exile it.
 *
 * @param grantPlayPermission whether the controller may cast the exiled card for as long as it
 *                            remains exiled
 */
public record ExileNonlandCardFromTargetHandOrGraveyardEffect(boolean grantPlayPermission)
        implements CardEffect {

    public ExileNonlandCardFromTargetHandOrGraveyardEffect() {
        this(true);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.player());
    }
}
