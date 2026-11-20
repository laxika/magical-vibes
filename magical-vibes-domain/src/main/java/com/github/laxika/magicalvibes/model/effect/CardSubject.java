package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

import java.util.Objects;

/**
 * Which cards outside the battlefield an effect acts on, the third subject type beside {@link PermanentSubject} and
 * {@link PlayerSubject}: "the exiled card", "each card put into a graveyard this way", "target creature card in your
 * graveyard". Built through {@link Subjects}; resolved only by the engine's {@code SubjectResolutionService}, which
 * names each card as it is now, in the public zone it is expected to be in, and nothing for a card that has left that
 * zone (a card that moves is a new object, CR 400.7).
 *
 * <p>Only a {@link TargetCard} declares a target ({@link #declaresTarget()}). Every variant reads a stack entry, so a
 * static ability can't use one.
 *
 * <p>The variants and the wording each stands for are listed in {@code agent-docs/SUBJECTS_REFERENCE.md}.
 */
public sealed interface CardSubject permits CardSubject.CardsThisWay, CardSubject.TargetCard {

    /** Whether this subject is the effect's own card target, chosen as the spell or ability is put on the stack. */
    default boolean declaresTarget() {
        return false;
    }

    /**
     * The cards an earlier step of the same resolution moved: for {@link StepKind#EXILE} the cards it exiled, in exile;
     * for {@link StepKind#DESTROY} and {@link StepKind#SACRIFICE} the cards the permanents it removed became in the
     * zone they went to (a token, which ceases to exist, becomes none); for {@link StepKind#DISCARD} the discarded
     * cards, in the graveyard or the public zone a replacement put them in instead; for {@link StepKind#COUNTER_SPELL}
     * the card the countered spell became in its graveyard or in exile (a copy, an ability or a card put into a hidden
     * zone becomes none). The rest of the effect can find a card it moved to a public zone (CR 400.7j); a card that has
     * left that zone since is a new object and is not named.
     *
     * <p>With a {@code filter}, only the cards matching it as they are now: "that card" after "exile cards from the top
     * of your library until you exile a nonland card" is the one nonland card among them.
     *
     * @param kind   the step kind whose most recent step is read
     * @param filter which of the moved cards are named; {@code null} for all of them
     */
    record CardsThisWay(StepKind kind, CardPredicate filter) implements CardSubject {

        public CardsThisWay(StepKind kind) {
            this(kind, null);
        }

        public CardsThisWay {
            Objects.requireNonNull(kind, "kind");
            switch (kind) {
                case EXILE, DESTROY, SACRIFICE, DISCARD, COUNTER_SPELL -> {
                }
                default -> throw new IllegalArgumentException("A " + kind + " step leaves no card for a card "
                        + "subject to name");
            }
        }
    }

    /**
     * "Target [filter] card in a graveyard": the card the effect targets in a graveyard of {@code scope}, while it is
     * still there and still matches {@code filter} (CR 608.2b). The effect reading it declares the target.
     */
    record TargetCard(CardPredicate filter, GraveyardSearchScope scope) implements CardSubject {

        public TargetCard {
            Objects.requireNonNull(scope, "scope");
        }

        @Override
        public boolean declaresTarget() {
            return true;
        }
    }
}
