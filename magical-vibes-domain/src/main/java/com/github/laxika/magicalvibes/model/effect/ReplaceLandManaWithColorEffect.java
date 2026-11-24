package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * "If a land is tapped for mana, it produces [color] instead of any other type": a replacement of the
 * mana production event while the land's mana ability resolves (CR 614.1a, 106.12b). The land keeps its
 * mana abilities; only the mana they make changes.
 *
 * <p>As a static ability ({@code duration == null}) it applies while its source is on the battlefield
 * with its abilities (Infernal Darkness, Contamination, Pulse of Llanowar). As a spell's or ability's
 * instruction it is floated for {@code duration} (Deep Water, Pale Moon, Chaos Moon, Harvest Mage),
 * on every land {@code lands} matches, or on each land {@code subject} names (Quarum Trench Gnomes'
 * target). "You" is the static ability's or the floating effect's controller. Read by
 * {@code GameQueryService.landManaColorReplacements}, which the land-mana replacement pool and the
 * mana previews ask.
 *
 * @param color         the type the land produces instead; {@code null} for "a color of your choice",
 *                      chosen by the tapping player for each tap
 * @param fixedAmount   the amount it produces instead ("instead of any other type and amount"), or
 *                      {@code null} to keep the amount
 * @param lands         which lands are affected, judged with the effect's controller as "you" as each land is
 *                      tapped; {@code null} for every land. With a target subject it only restricts the targets:
 *                      the floated replacement then follows the targeted land itself.
 * @param replacedTypes the types replaced ("instead of white mana", "instead of any other color"); empty
 *                      for "instead of any other type". A tap that makes none of them isn't replaced.
 * @param onlyYourTaps  "if you tap a land": only taps by the effect's controller are replaced
 * @param subject       {@code null} for every land {@code lands} matches; a target subject floats it on
 *                      each targeted land
 * @param duration      {@code null} for a static ability; else how long the floated replacement lasts
 */
public record ReplaceLandManaWithColorEffect(ManaColor color, Integer fixedAmount, PermanentPredicate lands,
                                             Set<ManaColor> replacedTypes, boolean onlyYourTaps,
                                             PermanentSubject subject, Duration duration) implements CardEffect {

    public ReplaceLandManaWithColorEffect {
        replacedTypes = replacedTypes == null || replacedTypes.isEmpty()
                ? Set.of() : Set.copyOf(EnumSet.copyOf(replacedTypes));
    }

    /** Static: "if a land is tapped for mana, it produces {@code color} instead of any other type". */
    public ReplaceLandManaWithColorEffect(ManaColor color) {
        this(color, null);
    }

    /** Static, with "instead of any other type and amount" when {@code fixedAmount} is set (Contamination). */
    public ReplaceLandManaWithColorEffect(ManaColor color, Integer fixedAmount) {
        this(color, fixedAmount, null, Set.of(), false, null, null);
    }

    /** "It produces mana of a color of your choice instead of any other type": the tapping player chooses. */
    public static ReplaceLandManaWithColorEffect colorOfYourChoice() {
        return new ReplaceLandManaWithColorEffect(null);
    }

    /** Only the lands {@code filter} matches ("a basic land", "a Mountain", "a nonbasic land"). */
    public ReplaceLandManaWithColorEffect forLands(PermanentPredicate filter) {
        PermanentPredicate combined = lands == null ? filter : new PermanentAllOfPredicate(List.of(lands, filter));
        return new ReplaceLandManaWithColorEffect(color, fixedAmount, combined, replacedTypes, onlyYourTaps,
                subject, duration);
    }

    /** "A land you control": lands the effect's controller controls. */
    public ReplaceLandManaWithColorEffect youControl() {
        return forLands(new PermanentControlledBySourceControllerPredicate());
    }

    /** "If you tap a land for mana": only taps by the effect's controller. */
    public ReplaceLandManaWithColorEffect whenYouTap() {
        return new ReplaceLandManaWithColorEffect(color, fixedAmount, lands, replacedTypes, true, subject, duration);
    }

    /** "Instead of [these types]": only mana of {@code types} is replaced, other mana the land makes stays. */
    public ReplaceLandManaWithColorEffect replacing(Set<ManaColor> types) {
        return new ReplaceLandManaWithColorEffect(color, fixedAmount, lands, types, onlyYourTaps, subject, duration);
    }

    /** "Instead of any other type and amount": the land produces {@code amount} mana. */
    public ReplaceLandManaWithColorEffect producing(int amount) {
        return new ReplaceLandManaWithColorEffect(color, amount, lands, replacedTypes, onlyYourTaps, subject,
                duration);
    }

    /** As an instruction: floats the replacement for {@code duration} ("until end of turn"). */
    public ReplaceLandManaWithColorEffect lasting(Duration duration) {
        return new ReplaceLandManaWithColorEffect(color, fixedAmount, lands, replacedTypes, onlyYourTaps, subject,
                duration);
    }

    /** As an instruction on the lands {@code subject} names ("if target Plains is tapped for mana"). */
    public ReplaceLandManaWithColorEffect on(PermanentSubject subject) {
        return new ReplaceLandManaWithColorEffect(color, fixedAmount, lands, replacedTypes, onlyYourTaps, subject,
                duration);
    }

    @Override
    public TargetSpec targetSpec() {
        if (subject == null || !subject.declaresTarget()) {
            return TargetSpec.NONE;
        }
        return lands == null ? TargetSpec.benign(TargetPredicates.land())
                : TargetSpec.benign(TargetPredicates.land(), lands);
    }
}
