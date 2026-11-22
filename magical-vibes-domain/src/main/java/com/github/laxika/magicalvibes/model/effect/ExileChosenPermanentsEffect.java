package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * "Exile a [filter] you control", "exile a Human you control and an artifact you control": the controller chooses one
 * permanent they control for each choice as the effect resolves, a different one each, and they are exiled together.
 * Nothing is exiled when a choice has no permanent left for it. The step pauses at its end on the choice and records
 * its {@code StepKind.EXILE} as it is answered, with each permanent as it last existed, so "if you do", "when you do"
 * and "the exiled creature's power" are later steps.
 *
 * @param choices what is chosen, in the order it is asked
 * @param link    how the exiled cards are linked to the source permanent
 */
public record ExileChosenPermanentsEffect(List<Choice> choices, SourceLink link) implements CardEffect {

    /**
     * One permanent to choose.
     *
     * @param filter which permanents the controller may choose among those they control
     * @param label  what the prompt calls one ("land", "nontoken creature you control")
     */
    public record Choice(PermanentPredicate filter, String label) {

        public Choice {
            Objects.requireNonNull(filter, "filter");
            Objects.requireNonNull(label, "label");
        }
    }

    /** How the exiled cards are linked to the source permanent. */
    public enum SourceLink {
        /** Not linked. */
        NONE,
        /** "The exiled card" of the source's other abilities. */
        EXILED_WITH_SOURCE,
        /**
         * "Until this leaves the battlefield": linked and returned as it leaves; nothing is exiled when it has already
         * left (CR 610.3a, 610.3b).
         */
        UNTIL_SOURCE_LEAVES
    }

    public ExileChosenPermanentsEffect {
        choices = List.copyOf(choices);
        Objects.requireNonNull(link, "link");
        if (choices.isEmpty()) {
            throw new IllegalArgumentException("Something is chosen");
        }
    }

    /** "Exile a [filter] you control." */
    public static ExileChosenPermanentsEffect youControl(PermanentPredicate filter, String label) {
        return new ExileChosenPermanentsEffect(List.of(new Choice(filter, label)), SourceLink.NONE);
    }

    /** "... and a [filter] you control." */
    public ExileChosenPermanentsEffect and(PermanentPredicate filter, String label) {
        List<Choice> all = new ArrayList<>(choices);
        all.add(new Choice(filter, label));
        return new ExileChosenPermanentsEffect(all, link);
    }

    /** These permanents exiled with the source. */
    public ExileChosenPermanentsEffect withSource() {
        return new ExileChosenPermanentsEffect(choices, SourceLink.EXILED_WITH_SOURCE);
    }

    /** These permanents exiled until the source leaves the battlefield. */
    public ExileChosenPermanentsEffect untilSourceLeaves() {
        return new ExileChosenPermanentsEffect(choices, SourceLink.UNTIL_SOURCE_LEAVES);
    }
}
