package com.github.laxika.magicalvibes.model;

/**
 * A queued player decision: everything the engine needs to prompt the deciding player and
 * apply their answer later. Instances wait in {@link GameData#pendingInteractions} until
 * serviced; consumers scan the queue for the first entry of the kind they handle (see the
 * type-filtered helpers on {@link GameData}), which preserves FIFO order per kind.
 *
 * <p>This is the unification point for the legacy pending-choice subsystem: the
 * {@link PermanentChoiceContext} records are the first members, and the remaining
 * {@code Pending*} / {@code ChoiceContext} shapes migrate here incrementally
 * (see {@code REFACTOR-NOTES.md} at the repository root).
 */
public sealed interface PendingInteraction permits PermanentChoiceContext {
}
