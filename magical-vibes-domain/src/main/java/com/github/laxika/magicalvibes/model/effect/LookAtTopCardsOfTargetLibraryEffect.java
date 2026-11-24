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
 * <p>{@link #lookingAt} names the libraries instead ("look at the top card of your library", "of each player's
 * library", "of that player's library" after another step declared the player target): a {@code LOOK_ONLY} look
 * at each named player's library in APNAP order, private to the controller (CR 701.20e's look; a hidden card stays
 * "a card" to everyone else), with no prompt and no rearranging. It records nothing.
 *
 * @param graveyardCount number of cards chosen together for the graveyard actions (defaults to one)
 * @param libraries      whose libraries a {@code LOOK_ONLY} look is at, or {@code null} for the entry's target (else
 *                       its controller)
 */
public record LookAtTopCardsOfTargetLibraryEffect(DynamicAmount count, TargetLibraryAction action,
                                                  int graveyardCount, PlayerSubject libraries)
        implements CardEffect {

    public LookAtTopCardsOfTargetLibraryEffect {
        if (libraries != null && action != TargetLibraryAction.LOOK_ONLY) {
            throw new IllegalArgumentException("Only a LOOK_ONLY look names its libraries: " + action);
        }
    }

    public LookAtTopCardsOfTargetLibraryEffect(DynamicAmount count, TargetLibraryAction action, int graveyardCount) {
        this(count, action, graveyardCount, null);
    }

    public LookAtTopCardsOfTargetLibraryEffect(DynamicAmount count, TargetLibraryAction action) {
        this(count, action, 1);
    }

    /**
     * "Look at the top [count] card(s) of [libraries]": {@code you()} (Delver of Secrets), {@code eachPlayer()} (Case the
     * Joint), {@code targetPlayerDeclaredElsewhere()} (Spy Network); only {@code targetPlayer()} declares a target.
     */
    public static LookAtTopCardsOfTargetLibraryEffect lookingAt(int count, PlayerSubject libraries) {
        return new LookAtTopCardsOfTargetLibraryEffect(new Fixed(count), TargetLibraryAction.LOOK_ONLY, 1,
                java.util.Objects.requireNonNull(libraries, "libraries"));
    }

    public LookAtTopCardsOfTargetLibraryEffect(int count, TargetLibraryAction action) {
        this(new Fixed(count), action);
    }

    public LookAtTopCardsOfTargetLibraryEffect(int count, TargetLibraryAction action, int graveyardCount) {
        this(new Fixed(count), action, graveyardCount);
    }

    @Override
    public TargetSpec targetSpec() {
        if (libraries != null) {
            return libraries.declaresTarget() ? TargetSpec.benign(TargetPredicates.player()) : TargetSpec.NONE;
        }
        return switch (action) {
            case MAY_SHUFFLE -> TargetSpec.benign(TargetPredicates.player());
            case KEEP_ONE_ON_TOP_EXILE_REST, KEEP_ONE_ON_TOP_REST_TO_GRAVEYARD,
                    MAY_PUT_TOP_ON_BOTTOM, EXILE_ONE_FACE_DOWN_REST_TO_BOTTOM_RANDOM,
                    EXILE_ONE_FACE_DOWN_CAST_ONLY_REST_TO_BOTTOM_RANDOM,
                    EXILE_TWO_FACE_DOWN_REST_TO_BOTTOM_RANDOM,
                    EXILE_ONE_FACE_DOWN_REST_TO_GRAVEYARD,
                    MAY_PUT_NONLAND_PERMANENT_WITH_MANA_VALUE_X_ONTO_BATTLEFIELD ->
                    TargetSpec.harmful(TargetPredicates.player());
            default -> CardEffect.super.targetSpec();
        };
    }
}
