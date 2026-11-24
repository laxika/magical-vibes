package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardColor;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

/**
 * "[Permanents] become [colors]" made by a resolving spell or ability: a layer-5 colour-setting effect that replaces
 * the permanents' colours (CR 105.3) for {@link #duration}. The permanents are those {@link #subject} names as it
 * resolves, kept for the effect's whole duration (CR 611.2c).
 *
 * <p>{@link #choice} says where the colours come from:
 * <ul>
 *   <li>{@link ColorChoice#FIXED}: {@link #colors} as written ("becomes all colors", "becomes blue"); an empty set is
 *       "becomes colorless".</li>
 *   <li>{@link ColorChoice#ONE_OR_MORE}: "the color or colors of your choice" (Prismwake Merrow, Prismatic Lace).</li>
 *   <li>{@link ColorChoice#ONE}: "the color of your choice" (Sway of Illusion).</li>
 * </ul>
 * A chosen form asks its controller as it resolves, once for every permanent it names, and asks nothing when it names
 * none. The colours applied, chosen or fixed, are floated as a {@link ColorChoice#FIXED} instance of this record,
 * which the CR 613 layered pass applies at its own timestamp (see {@code LayerSystemService.applyL5Instance}).
 *
 * <p>The no-argument form is "target permanent becomes the color or colors of your choice until end of turn".
 */
public record BecomeChosenColorsUntilEndOfTurnEffect(Set<CardColor> colors, PermanentSubject subject,
                                                     Duration duration, ColorChoice choice) implements CardEffect {

    /** Where the colours of a {@link BecomeChosenColorsUntilEndOfTurnEffect} come from. */
    public enum ColorChoice {
        /** The colours the record carries. */
        FIXED,
        /** One colour the controller chooses. */
        ONE,
        /** One or more colours the controller chooses. */
        ONE_OR_MORE
    }

    public BecomeChosenColorsUntilEndOfTurnEffect {
        colors = new LinkedHashSet<>(colors);
        Objects.requireNonNull(subject, "subject");
        Objects.requireNonNull(duration, "duration");
        Objects.requireNonNull(choice, "choice");
    }

    /** "Target permanent becomes the color or colors of your choice until end of turn". */
    public BecomeChosenColorsUntilEndOfTurnEffect() {
        this(Set.of(), Subjects.target(), Duration.UNTIL_END_OF_TURN, ColorChoice.ONE_OR_MORE);
    }

    /** The floated setter: the permanent it is anchored on is exactly {@code colors} (none: colorless). */
    public BecomeChosenColorsUntilEndOfTurnEffect(Set<CardColor> colors) {
        this(colors, Subjects.target(), Duration.UNTIL_END_OF_TURN, ColorChoice.FIXED);
    }

    /** "[Subject] becomes the color or colors of your choice", for {@code duration}. */
    public static BecomeChosenColorsUntilEndOfTurnEffect chosenColors(PermanentSubject subject, Duration duration) {
        return new BecomeChosenColorsUntilEndOfTurnEffect(Set.of(), subject, duration, ColorChoice.ONE_OR_MORE);
    }

    /** "[Subject] becomes the color of your choice", for {@code duration}. */
    public static BecomeChosenColorsUntilEndOfTurnEffect chosenColor(PermanentSubject subject, Duration duration) {
        return new BecomeChosenColorsUntilEndOfTurnEffect(Set.of(), subject, duration, ColorChoice.ONE);
    }

    /** "[Subject] becomes [colors]" (no colours: "becomes colorless"), for {@code duration}. */
    public static BecomeChosenColorsUntilEndOfTurnEffect fixed(Set<CardColor> colors, PermanentSubject subject,
                                                               Duration duration) {
        return new BecomeChosenColorsUntilEndOfTurnEffect(colors, subject, duration, ColorChoice.FIXED);
    }

    @Override
    public TargetSpec targetSpec() {
        return subject.declaresTarget() ? TargetSpec.benign(TargetPredicates.permanent()) : TargetSpec.NONE;
    }
}
