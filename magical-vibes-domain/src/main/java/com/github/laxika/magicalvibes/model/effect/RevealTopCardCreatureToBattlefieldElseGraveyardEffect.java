package com.github.laxika.magicalvibes.model.effect;

/**
 * Reveals the top card of the controller's library. If it's a creature card, put it onto the
 * battlefield. Otherwise, put it into the controller's graveyard.
 *
 * <p>When {@code grantHaste} is true the entering creature gains haste until end of turn; when
 * {@code sacrificeAtEndStep} is true it is sacrificed at the beginning of the next end step
 * (Impromptu Raid). Both false for the plain Call of the Wild variant.
 */
public record RevealTopCardCreatureToBattlefieldElseGraveyardEffect(boolean grantHaste,
                                                                    boolean sacrificeAtEndStep)
        implements CardEffect {

    public RevealTopCardCreatureToBattlefieldElseGraveyardEffect() {
        this(false, false);
    }
}
