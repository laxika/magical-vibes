package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/**
 * Destroys the permanent linked to the source permanent — the one whose id the source recorded in
 * {@code Permanent.chosenPermanentId} (Merieke Ri Berit's "When this creature leaves the battlefield
 * or becomes untapped, destroy that creature", where "that creature" is the one she gained control
 * of via {@link GainControlOfTargetEffect#linkingToSource}).
 *
 * <p>Non-targeting: the permanent is predetermined, never chosen. The card holds the marker
 * instance with a {@code null} id; the {@code ON_SELF_LEAVES_BATTLEFIELD} collector bakes the
 * captured id in before the source is gone, while the {@code ON_SELF_BECOMES_UNTAPPED} trigger
 * carries a source permanent id the handler can still read the link from. Resolution clears the
 * link, so a second untap does not destroy anything.
 *
 * @param cannotBeRegenerated whether the destroyed permanent can't be regenerated
 * @param linkedPermanentId   the permanent to destroy, or {@code null} to read it from the source
 */
public record DestroyLinkedPermanentEffect(boolean cannotBeRegenerated, UUID linkedPermanentId)
        implements RemovalEffect {

    public DestroyLinkedPermanentEffect(boolean cannotBeRegenerated) {
        this(cannotBeRegenerated, null);
    }

    @Override
    public RemovalKind removalKind() {
        return RemovalKind.DESTROY;
    }
}
