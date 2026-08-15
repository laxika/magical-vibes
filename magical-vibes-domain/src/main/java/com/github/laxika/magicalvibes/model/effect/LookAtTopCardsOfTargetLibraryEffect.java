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
 * {@code target(...)} / activated-ability player filters. {@code MAY_SHUFFLE},
 * {@code KEEP_ONE_ON_TOP_EXILE_REST} and {@code MAY_PUT_TOP_ON_BOTTOM} carry a {@link TargetSpec}
 * of their own (the latter so a targeted trigger such as Precognition's upkeep ability routes
 * through the player-target pipeline); the other actions keep the record targeting-neutral. Eye Spy / Wand of Denial's "look at top card, may put it into the graveyard
 * with a cost" stays a separate record ({@link LookAtTargetPlayerTopCardMayGraveyardEffect}) —
 * it re-pushes itself as a costed may-ability, a different mechanism.
 *
 * @param count  how many cards to look at from the top of the target player's library; any
 *               {@link DynamicAmount} — {@link Fixed} for a printed number, {@code XValue} for an
 *               {@code {X}} spell (Sealed Fate)
 * @param action what the controller does with the looked-at cards
 */
public record LookAtTopCardsOfTargetLibraryEffect(DynamicAmount count, TargetLibraryAction action)
        implements CombatDamageTriggerContextEffect {

    public LookAtTopCardsOfTargetLibraryEffect(int count, TargetLibraryAction action) {
        this(new Fixed(count), action);
    }

    @Override
    public TargetSpec targetSpec() {
        return switch (action) {
            case MAY_SHUFFLE -> TargetSpec.benign(TargetPredicates.player());
            case PUT_ONE_INTO_GRAVEYARD, REVEAL_AND_PUT_ONE_INTO_GRAVEYARD,
                    KEEP_ONE_ON_TOP_EXILE_REST, KEEP_ONE_ON_TOP_REST_TO_GRAVEYARD,
                    MAY_PUT_TOP_ON_BOTTOM, EXILE_ONE_FACE_DOWN_REST_TO_BOTTOM_RANDOM ->
                    TargetSpec.harmful(TargetPredicates.player());
            default -> CombatDamageTriggerContextEffect.super.targetSpec();
        };
    }

    @Override
    public TriggerContext combatDamageTriggerContext() {
        return TriggerContext.DAMAGED_PLAYER;
    }
}
