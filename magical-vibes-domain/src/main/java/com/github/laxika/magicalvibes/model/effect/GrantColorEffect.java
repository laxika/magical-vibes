package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Static effect that grants a color to permanents matching the given subject.
 * For equipment: "equipped creature is a black [type]" adds the color.
 * For auras: "enchanted creature is [color]" similarly.
 * With {@link Subjects#target()}, it can also be used by a resolving ability to permanently
 * set the target's colors when {@code overriding} is true.
 *
 * <p>{@link #chosenColor(PermanentSubject, PermanentPredicate)} is "[permanents] are the chosen color": the color the
 * source stored as its choice ({@code Permanent.getChosenColor()}, from {@link ChooseColorOnEnterEffect} or
 * {@link ChooseColorForSourceEffect}), replacing their colors, and nothing until a color is chosen. Over
 * {@link Subjects#source()} it is not a characteristic-defining ability: it takes the timestamp of the choice when
 * that is later than the permanent's (Puca's Eye's "this artifact becomes the chosen color").
 *
 * @param color         the color to grant ({@code null} with {@code ofChosenColor})
 * @param subject       which permanents are affected (EQUIPPED_CREATURE, ENCHANTED_CREATURE, etc.)
 * @param overriding    when true, replaces all existing colors instead of adding (e.g. "is a black Zombie")
 * @param filter        optional predicate to restrict which permanents are affected
 * @param ofChosenColor whether the color is the source's chosen color instead of {@code color}
 */
public record GrantColorEffect(CardColor color, PermanentSubject subject, boolean overriding,
                               PermanentPredicate filter, boolean ofChosenColor) implements CardEffect {

    public GrantColorEffect(CardColor color, PermanentSubject subject, boolean overriding,
                            PermanentPredicate filter) {
        this(color, subject, overriding, filter, false);
    }

    public GrantColorEffect(CardColor color, PermanentSubject subject) {
        this(color, subject, false, null);
    }

    public GrantColorEffect(CardColor color, PermanentSubject subject, boolean overriding) {
        this(color, subject, overriding, null);
    }

    /** "[Subject] is the chosen color" (replacing its colors), the color being the source's stored choice. */
    public static GrantColorEffect chosenColor(PermanentSubject subject) {
        return chosenColor(subject, null);
    }

    /** "[Subject] matching {@code filter} are the chosen color" (replacing their colors). */
    public static GrantColorEffect chosenColor(PermanentSubject subject, PermanentPredicate filter) {
        return new GrantColorEffect(null, subject, true, filter, true);
    }

    @Override
    public TargetSpec targetSpec() {
        return subject instanceof PermanentSubject.Target
                ? TargetSpec.benign(TargetPredicates.permanent())
                : TargetSpec.NONE;
    }
}
