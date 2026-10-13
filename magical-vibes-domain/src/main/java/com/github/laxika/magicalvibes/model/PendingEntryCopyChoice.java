package com.github.laxika.magicalvibes.model;

import java.util.Set;
import java.util.UUID;

/**
 * An entry waiting for its own "enter as a copy" choice while another copy choice is open.
 * {@code copyablePermanentIds} are the permanents that were on the battlefield as it entered; anything
 * entering with it or after it can't be copied.
 */
public record PendingEntryCopyChoice(BattlefieldEntryRequest request, Set<UUID> copyablePermanentIds) {

    public PendingEntryCopyChoice {
        copyablePermanentIds = Set.copyOf(copyablePermanentIds);
    }

    public PendingEntryCopyChoice deepCopy() {
        return new PendingEntryCopyChoice(request.deepCopy(), copyablePermanentIds);
    }
}
