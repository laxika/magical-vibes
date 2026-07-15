package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/**
 * Look at the top card of target player's library. The spell/ability's controller
 * may put that card into that player's graveyard. Models Eye Spy (free, any card)
 * and Wand of Denial ({@code nonlandOnly=true}, {@code lifeCost=2}).
 *
 * <p>Resolved in two stages: on the card definition {@code libraryOwnerId} is
 * {@code null} and {@link #canTargetPlayer()} drives player targeting. When the
 * spell/ability resolves, the handler re-pushes a copy carrying the resolved
 * target player id (and the same restriction/cost) as a may-ability so the
 * controller can decide.
 *
 * @param libraryOwnerId the target player whose top card is looked at (null on the card definition)
 * @param nonlandOnly    when true, the may-ability is only offered if the top card is a nonland card
 * @param lifeCost       life the controller must pay to put the card into the graveyard (0 = free)
 */
public record LookAtTargetPlayerTopCardMayGraveyardEffect(UUID libraryOwnerId, boolean nonlandOnly, int lifeCost)
        implements CardEffect {

    /** Eye Spy — free, any card. */
    public LookAtTargetPlayerTopCardMayGraveyardEffect() {
        this(null, false, 0);
    }

    /** Card-definition constructor for a conditional/costed variant (e.g. Wand of Denial). */
    public LookAtTargetPlayerTopCardMayGraveyardEffect(boolean nonlandOnly, int lifeCost) {
        this(null, nonlandOnly, lifeCost);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetCategory.PLAYER);
    }
}
