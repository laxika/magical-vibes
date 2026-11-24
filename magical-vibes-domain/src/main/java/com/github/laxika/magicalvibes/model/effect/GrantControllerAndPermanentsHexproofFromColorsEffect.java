package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardColor;

import java.util.Objects;
import java.util.Set;

/**
 * Until end of turn, [the controller and] the permanents {@link #permanents} names gain hexproof from the given colors
 * (CR 702.11d). The permanents are found once as the effect resolves (CR 611.2c); each gets a floating copy of this
 * record carrying only the colors, which the hexproof-from-color readers ask for.
 *
 * <p>{@link #ofChosenColor} is "hexproof from that color" after "choose a color": the color an earlier
 * {@link ChooseColorAtResolutionEffect} of the same resolution chose, fixed as the effect applies (CR 608.2h). With no
 * color chosen nothing is granted.
 *
 * @param colors        the colors, empty with {@code ofChosenColor}
 * @param controllerToo whether the controller gains it too ("you and permanents you control", Veil of Summer)
 * @param permanents    the permanents that gain it: {@link Subjects#permanentsYouControl()}, {@link Subjects#target()}
 *                      or {@link Subjects#source()}
 * @param ofChosenColor whether the color is the one chosen earlier in this resolution instead of {@code colors}
 */
public record GrantControllerAndPermanentsHexproofFromColorsEffect(Set<CardColor> colors, boolean controllerToo,
                                                                  PermanentSubject permanents, boolean ofChosenColor)
        implements CardEffect {

    public GrantControllerAndPermanentsHexproofFromColorsEffect {
        colors = Set.copyOf(colors);
        Objects.requireNonNull(permanents, "permanents");
    }

    /** "You and permanents you control gain hexproof from {@code colors} until end of turn" (Veil of Summer). */
    public GrantControllerAndPermanentsHexproofFromColorsEffect(Set<CardColor> colors) {
        this(colors, true, Subjects.permanentsYouControl(), false);
    }

    /**
     * "[Permanent] gains hexproof from that color until end of turn" after "choose a color" (Mondo Gecko, Skrelv,
     * Sungold Sentinel).
     */
    public static GrantControllerAndPermanentsHexproofFromColorsEffect chosenColor(PermanentSubject permanents) {
        return new GrantControllerAndPermanentsHexproofFromColorsEffect(Set.of(), false, permanents, true);
    }

    @Override
    public TargetSpec targetSpec() {
        return permanents.declaresTarget() ? TargetSpec.benign(TargetPredicates.creature()) : TargetSpec.NONE;
    }
}
