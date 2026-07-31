package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

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
 * @param count  how many cards to look at from the top of the target player's library; any
 *               {@link DynamicAmount} — {@link Fixed} for a printed number, {@code XValue} for an
 *               {@code {X}} spell (Sealed Fate)
 * @param action what the controller does with the looked-at cards
 */
public record LookAtTopCardsOfTargetLibraryEffect(DynamicAmount count, TargetLibraryAction action)
        implements CardEffect {

    public LookAtTopCardsOfTargetLibraryEffect(int count, TargetLibraryAction action) {
        this(new Fixed(count), action);
    }

    @Override
    public TargetSpec targetSpec() {
        return switch (action) {
            case MAY_SHUFFLE -> TargetSpec.benign(TargetCategory.PLAYER);
            case KEEP_ONE_ON_TOP_EXILE_REST -> TargetSpec.harmful(TargetCategory.PLAYER);
            default -> CardEffect.super.targetSpec();
        };
    }
}
