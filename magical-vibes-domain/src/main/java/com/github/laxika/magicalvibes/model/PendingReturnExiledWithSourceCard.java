package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;

import java.util.UUID;

/**
 * Marks that the active {@code LIBRARY_REVEAL_CHOICE} is the controller choosing one card exiled
 * "with" a source permanent to return from exile (Endless Horizons upkeep, Purgatory upkeep). Only
 * the selected card leaves exile; the rest stay exiled.
 *
 * <p>{@code toBattlefield} sends the chosen card to the battlefield instead of the hand; it enters
 * under {@code controllerId}'s control per CR 110.2a, which may differ from its owner. An optional
 * as-enters replacement is used by effects that first exile a group and then choose from those cards.
 */
public record PendingReturnExiledWithSourceCard(boolean toBattlefield, UUID controllerId,
                                                CardSubtype grantedSubtype, boolean enterTapped,
                                                boolean enterAttacking, boolean grantHaste,
                                                EnterWithCountersEffect battlefieldEntryReplacement)
        implements PendingInteraction {

    public PendingReturnExiledWithSourceCard(boolean toBattlefield, UUID controllerId) {
        this(toBattlefield, controllerId, null, false, false, false, null);
    }

    public PendingReturnExiledWithSourceCard(boolean toBattlefield, UUID controllerId,
                                             CardSubtype grantedSubtype) {
        this(toBattlefield, controllerId, grantedSubtype, false, false, false, null);
    }

    public PendingReturnExiledWithSourceCard(boolean toBattlefield, UUID controllerId,
                                             CardSubtype grantedSubtype, boolean enterTapped,
                                             boolean enterAttacking, boolean grantHaste) {
        this(toBattlefield, controllerId, grantedSubtype, enterTapped, enterAttacking,
                grantHaste, null);
    }

    public PendingReturnExiledWithSourceCard(boolean toBattlefield, UUID controllerId,
                                             EnterWithCountersEffect battlefieldEntryReplacement) {
        this(toBattlefield, controllerId, null, false, false, false, battlefieldEntryReplacement);
    }

    public PendingReturnExiledWithSourceCard() {
        this(false, null, null, false, false, false, null);
    }
}
