package com.github.laxika.magicalvibes.model.effect;

/**
 * Look at the top {@code count} cards of target player's library, then apply {@code action}
 * (see {@link TargetLibraryAction}). Collapses the target-library look family: the pure peek
 * (Dewdrop Spy count 1, Orcish Spy count 3, Moonring Island), may-exile-one (Psychic Surgery,
 * Puresight Merrow), may-shuffle (Visions) and put-one-into-graveyard (Cruel Fate, Wu Spy).
 *
 * <p>The target player is the stack entry's {@code targetId}; cards declare the player target via
 * {@code target(...)} / activated-ability player filters. Only {@code MAY_SHUFFLE} carries a
 * {@link TargetSpec} of its own (as its old record did); the other actions keep the record
 * targeting-neutral. Eye Spy / Wand of Denial's "look at top card, may put it into the graveyard
 * with a cost" stays a separate record ({@link LookAtTargetPlayerTopCardMayGraveyardEffect}) —
 * it re-pushes itself as a costed may-ability, a different mechanism.
 *
 * @param count  how many cards to look at from the top of the target player's library
 * @param action what the controller does with the looked-at cards
 */
public record LookAtTopCardsOfTargetLibraryEffect(int count, TargetLibraryAction action)
        implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return action == TargetLibraryAction.MAY_SHUFFLE
                ? TargetSpec.benign(TargetCategory.PLAYER)
                : CardEffect.super.targetSpec();
    }
}
