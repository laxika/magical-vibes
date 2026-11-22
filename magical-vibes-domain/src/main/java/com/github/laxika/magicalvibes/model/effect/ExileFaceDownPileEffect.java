package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.Objects;

/**
 * "Exile [these] in a face-down pile, shuffle that pile, then [manifest | cloak] those cards": the cards are exiled
 * face down and enter together, face down, under the effect's controller's control (CR 701.40a, 701.58a). Records
 * the manifest step.
 *
 * @param pile   what is exiled in the pile
 * @param turned how the cards are turned face down onto the battlefield
 * @param tapped "they enter tapped"
 */
public record ExileFaceDownPileEffect(Pile pile, FaceDown turned, boolean tapped) implements CardEffect {

    /** What a pile is made of. */
    public sealed interface Pile {
    }

    /** "It and the top [N] cards of your library": the permanents still there and as many cards as there are. */
    public record PermanentsAndTopCards(PermanentSubject permanents, int topCards) implements Pile {

        public PermanentsAndTopCards {
            Objects.requireNonNull(permanents, "permanents");
        }
    }

    /**
     * "Any number of [filter] you control", chosen as the effect resolves.
     *
     * @param filter which permanents the controller may choose
     * @param label  what the prompt calls them
     */
    public record AnyNumberYouControl(PermanentPredicate filter, String label) implements Pile {

        public AnyNumberYouControl {
            Objects.requireNonNull(filter, "filter");
            Objects.requireNonNull(label, "label");
        }
    }

    /** "All [filter] cards from [a player's] graveyard." */
    public record AllFromGraveyard(PlayerSubject player, CardPredicate filter) implements Pile {

        public AllFromGraveyard {
            Objects.requireNonNull(player, "player");
            Objects.requireNonNull(filter, "filter");
        }
    }

    /** How the cards of the pile are put onto the battlefield face down. */
    public enum FaceDown {
        /** CR 701.40. */
        MANIFEST,
        /** CR 701.58: ward {2} with it. */
        CLOAK
    }

    public ExileFaceDownPileEffect {
        Objects.requireNonNull(pile, "pile");
        Objects.requireNonNull(turned, "turned");
    }

    /** "... then manifest those cards." */
    public static ExileFaceDownPileEffect thenManifest(Pile pile) {
        return new ExileFaceDownPileEffect(pile, FaceDown.MANIFEST, false);
    }

    /** "... then cloak those cards." */
    public static ExileFaceDownPileEffect thenCloak(Pile pile) {
        return new ExileFaceDownPileEffect(pile, FaceDown.CLOAK, false);
    }

    /** "They enter tapped." */
    public ExileFaceDownPileEffect enteringTapped() {
        return new ExileFaceDownPileEffect(pile, turned, true);
    }

    @Override
    public TargetSpec targetSpec() {
        if (pile instanceof PermanentsAndTopCards cards && cards.permanents().declaresTarget()) {
            return TargetSpec.harmful(TargetPredicates.permanent());
        }
        if (pile instanceof AllFromGraveyard cards && cards.player().declaresTarget()) {
            return TargetSpec.benign(TargetPredicates.player());
        }
        return TargetSpec.NONE;
    }
}
