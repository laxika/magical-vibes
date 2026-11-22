package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

import java.util.Objects;

/**
 * "Return two creatures you control to their owner's hand", "return up to two lands you control", "return any number
 * of Plains you control", "return half the creatures they control, rounded up": the controller chooses among the
 * permanents {@code candidates} names as the effect resolves (CR 608.2d) and the chosen ones are returned together.
 * With no more candidates than must be chosen, all of them return unasked, unless the choice is {@link #announced()}.
 *
 * <p>The step pauses at its end on the choice and records its {@code StepKind.RETURN} as it is answered, each
 * permanent as it last existed on the battlefield: "for each Plains returned this way" is
 * {@code AmountThisWay(StepKind.RETURN)}. A single permanent to choose, whoever controls it, is
 * {@link ReturnPermanentControlledByPlayerToHandEffect}.
 *
 * @param candidates among which permanents; only a target player's set declares a (benign) player target
 * @param minimum    how many must be chosen, as far as there are that many
 * @param maximum    how many may be chosen; {@code null} for any number
 * @param label      what the prompt calls one ("land")
 * @param alwaysAsked whether the choice is put to the controller also when it has one possible answer
 */
public record ReturnChosenPermanentsToHandEffect(PermanentSubject candidates, DynamicAmount minimum,
                                                 DynamicAmount maximum, String label, boolean alwaysAsked)
        implements CardEffect {

    public ReturnChosenPermanentsToHandEffect {
        Objects.requireNonNull(candidates, "candidates");
        Objects.requireNonNull(minimum, "minimum");
        Objects.requireNonNull(label, "label");
    }

    /** "Return [count] [candidates]." */
    public static ReturnChosenPermanentsToHandEffect exactly(DynamicAmount count, PermanentSubject candidates,
                                                             String label) {
        return new ReturnChosenPermanentsToHandEffect(candidates, count, count, label, false);
    }

    /** "Return [count] [candidates]." */
    public static ReturnChosenPermanentsToHandEffect exactly(int count, PermanentSubject candidates, String label) {
        return exactly(new Fixed(count), candidates, label);
    }

    /** "Return up to [count] [candidates]." */
    public static ReturnChosenPermanentsToHandEffect upTo(int count, PermanentSubject candidates, String label) {
        return new ReturnChosenPermanentsToHandEffect(candidates, new Fixed(0), new Fixed(count), label, false);
    }

    /** "Return any number of [candidates]." */
    public static ReturnChosenPermanentsToHandEffect anyNumber(PermanentSubject candidates, String label) {
        return new ReturnChosenPermanentsToHandEffect(candidates, new Fixed(0), null, label, false);
    }

    /** This choice put to the controller also when it has one possible answer. */
    public ReturnChosenPermanentsToHandEffect announced() {
        return new ReturnChosenPermanentsToHandEffect(candidates, minimum, maximum, label, true);
    }

    @Override
    public TargetSpec targetSpec() {
        return candidates.declaresTarget() ? TargetSpec.benign(TargetPredicates.player()) : TargetSpec.NONE;
    }
}
