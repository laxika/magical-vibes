package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/**
 * Resolution half of a ninjutsu ability (CR 702.49a): "Put this card onto the battlefield from your
 * hand tapped and attacking."
 *
 * <p>The cost half — paying the mana and returning an unblocked attacking creature you control to
 * its owner's hand — is applied at activation time, which is also where {@code attackTargetId} is
 * captured from the returned attacker so the ninja enters attacking the same player or planeswalker
 * (CR 702.49c). The copy of this effect printed on the card carries a {@code null} target; the
 * activation path snapshots a copy carrying the real defender onto the stack.
 */
public record NinjutsuEffect(UUID attackTargetId) implements CardEffect {

    public NinjutsuEffect() {
        this(null);
    }
}
