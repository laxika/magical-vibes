package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.List;

/**
 * Static effect: each permanent {@code subject} names that matches {@code filter} is a creature in addition to its
 * other types, with base power and base toughness each equal to its mana value ("each noncreature artifact is an
 * artifact creature with power and toughness each equal to its mana value", March of the Machines; "each other
 * non-Aura enchantment is a creature in addition to its other types and has base power and base toughness each equal
 * to its mana value", Opalescence). The old types stay (CR 205.1b).
 *
 * <p>The type change applies in layer 4 (CR 613.1d) to the permanents the subject and filter name as of the layer-4
 * effects applied before it; the base P/T applies in sublayer 7b (CR 613.4b), and the optional ability loss in layer 6,
 * to that same set, even if they no longer match the filter or the source lost its abilities meanwhile (CR 613.6).
 * The layered pass keeps that set per effect instance.
 *
 * <p>With {@code losesAllAbilities} the animated permanents also lose all abilities (Titania's Song). Titania's Song's
 * "if this enchantment leaves the battlefield, this effect continues until end of turn" is read from the same flag,
 * as it is the only card that sets it.
 *
 * @param subject           which permanents the effect looks at ({@code allPermanents()}, {@code otherPermanents()})
 * @param filter            which of those it animates, judged in layer 4
 * @param losesAllAbilities whether the animated permanents also lose all abilities
 */
public record AnimateNoncreatureArtifactsEffect(PermanentSubject subject, PermanentPredicate filter,
                                                boolean losesAllAbilities) implements CardEffect {

    /** "Each noncreature artifact loses all abilities and becomes an artifact creature ..." (Titania's Song). */
    public AnimateNoncreatureArtifactsEffect(boolean losesAllAbilities) {
        this(Subjects.allPermanents(), noncreatureArtifact(), losesAllAbilities);
    }

    /** "Each noncreature artifact is an artifact creature ..." (March of the Machines). */
    public AnimateNoncreatureArtifactsEffect() {
        this(false);
    }

    /** "Each other non-Aura enchantment is a creature in addition to its other types ..." (Opalescence). */
    public static AnimateNoncreatureArtifactsEffect otherNonAuraEnchantments() {
        return new AnimateNoncreatureArtifactsEffect(Subjects.otherPermanents(), new PermanentAllOfPredicate(List.of(
                new PermanentIsEnchantmentPredicate(),
                new PermanentNotPredicate(new PermanentHasSubtypePredicate(CardSubtype.AURA)))), false);
    }

    private static PermanentPredicate noncreatureArtifact() {
        return new PermanentAllOfPredicate(List.of(new PermanentIsArtifactPredicate(),
                new PermanentNotPredicate(new PermanentIsCreaturePredicate())));
    }
}
