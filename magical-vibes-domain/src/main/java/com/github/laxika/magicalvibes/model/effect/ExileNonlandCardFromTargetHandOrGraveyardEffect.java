package com.github.laxika.magicalvibes.model.effect;

/**
 * Reveals the target player's hand, then lets the controller choose a nonland card from that
 * hand or that player's graveyard and exile it.
 *
 * @param grantPlayPermission whether the controller may cast the exiled card for as long as it
 *                            remains exiled
 * @param handOnly whether the chosen card must come from the target player's hand rather than
 *                 their hand or graveyard
 */
public record ExileNonlandCardFromTargetHandOrGraveyardEffect(boolean grantPlayPermission,
                                                               boolean handOnly)
        implements CardEffect {

    public ExileNonlandCardFromTargetHandOrGraveyardEffect() {
        this(true, false);
    }

    public ExileNonlandCardFromTargetHandOrGraveyardEffect(boolean grantPlayPermission) {
        this(grantPlayPermission, false);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.player());
    }
}
