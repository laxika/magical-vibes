package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/**
 * Look at the top card of target player's library. The spell's controller may
 * put that card into that player's graveyard. Models Eye Spy.
 *
 * <p>Resolved in two stages: on the card definition {@code libraryOwnerId} is
 * {@code null} and {@link #canTargetPlayer()} drives player targeting. When the
 * spell resolves, the handler re-pushes a copy carrying the resolved target
 * player id as a may-ability so the controller can decide.
 *
 * @param libraryOwnerId the target player whose top card is looked at (null on the card definition)
 */
public record LookAtTargetPlayerTopCardMayGraveyardEffect(UUID libraryOwnerId) implements CardEffect {

    public LookAtTargetPlayerTopCardMayGraveyardEffect() {
        this(null);
    }

    @Override
    public boolean canTargetPlayer() {
        return true;
    }
}
