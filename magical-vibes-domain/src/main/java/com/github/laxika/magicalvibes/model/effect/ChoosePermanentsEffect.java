package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

import java.util.Objects;

/**
 * "Choose a creature an opponent controls", "choose up to two creatures", "choose any number of creatures with
 * different powers": as the effect resolves its controller chooses among the permanents {@code candidates} names
 * (CR 608.2d); none of them is targeted. With no more candidates than must be chosen, all of them are, unasked.
 *
 * <p>A CHOOSE step that pauses at its end on the choice: {@code Subjects.thisWay(StepKind.CHOOSE)} is what was chosen,
 * for the steps after it. A source that chose one permanent also keeps it as its chosen permanent ("when the chosen
 * creature leaves the battlefield").
 *
 * @param candidates among which permanents; nothing here is targeted
 * @param minimum    how many must be chosen, as far as there are that many
 * @param maximum    how many may be chosen; {@code null} for any number
 * @param different  the quality no two of the chosen share, or {@code null}; only with no minimum
 * @param label      what is chosen, for the prompt ("up to two creatures")
 */
public record ChoosePermanentsEffect(PermanentSubject candidates, DynamicAmount minimum, DynamicAmount maximum,
                                     Different different, String label) implements CardEffect {

    /** "With different powers", "with different mana values". */
    public enum Different {
        POWER("powers"),
        MANA_VALUE("mana values");

        private final String plural;

        Different(String plural) {
            this.plural = plural;
        }

        public String plural() {
            return plural;
        }
    }

    public ChoosePermanentsEffect {
        Objects.requireNonNull(candidates, "candidates");
        Objects.requireNonNull(minimum, "minimum");
        Objects.requireNonNull(label, "label");
        if (candidates.declaresTarget()) {
            throw new IllegalArgumentException("A permanent chosen as the effect resolves is no target: " + candidates);
        }
        if (different != null && !minimum.equals(new Fixed(0))) {
            throw new IllegalArgumentException("Permanents with different qualities are any number: " + minimum);
        }
    }

    /** "Choose [count] [candidates]." */
    public static ChoosePermanentsEffect exactly(int count, PermanentSubject candidates, String label) {
        return new ChoosePermanentsEffect(candidates, new Fixed(count), new Fixed(count), null, label);
    }

    /** "Choose up to [count] [candidates]." */
    public static ChoosePermanentsEffect upTo(int count, PermanentSubject candidates, String label) {
        return new ChoosePermanentsEffect(candidates, new Fixed(0), new Fixed(count), null, label);
    }

    /** "Choose any number of [candidates]." */
    public static ChoosePermanentsEffect anyNumber(PermanentSubject candidates, String label) {
        return new ChoosePermanentsEffect(candidates, new Fixed(0), null, null, label);
    }

    /** These, no two with the same {@code quality}. */
    public ChoosePermanentsEffect withDifferent(Different quality) {
        return new ChoosePermanentsEffect(candidates, minimum, maximum, quality,
                label + " with different " + quality.plural());
    }
}
