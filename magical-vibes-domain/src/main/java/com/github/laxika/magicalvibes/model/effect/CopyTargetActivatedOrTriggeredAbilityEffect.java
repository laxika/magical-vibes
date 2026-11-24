package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

import java.util.Objects;

/**
 * "Copy target activated or triggered ability [you control] [N times]. You may choose new targets for the copies."
 * The copy keeps every decision made for the original: its targets, X and the objects used to pay its costs
 * (CR 707.10), and its controller may choose new targets for each copy (CR 707.10c). Which abilities may be targeted
 * (activated and/or triggered, "you control", "from an artifact source") is the ability's target filter. "Copy that
 * ability" of a trigger about an ability (Mirror-Shield Hoplite) is this over the trigger's event ability.
 *
 * @param copies       how many copies ("twice" {@code Fixed(2)}, Mister Fantastic; "X times" {@code XValue}, Gogo)
 * @param uncopyable   "This ability can't be copied" (Gogo, Master of Mimicry): an ability resolving this effect
 *                     can't itself be copied ({@code CopySupport.isAbilityThatCantBeCopied})
 */
public record CopyTargetActivatedOrTriggeredAbilityEffect(DynamicAmount copies, boolean uncopyable)
        implements CardEffect {

    public CopyTargetActivatedOrTriggeredAbilityEffect {
        Objects.requireNonNull(copies, "copies");
    }

    /** One copy. */
    public CopyTargetActivatedOrTriggeredAbilityEffect() {
        this(new Fixed(1), false);
    }

    /** "Copy target ... ability {@code copies} times." */
    public static CopyTargetActivatedOrTriggeredAbilityEffect times(DynamicAmount copies) {
        return new CopyTargetActivatedOrTriggeredAbilityEffect(copies, false);
    }

    /** This effect on an ability that "can't be copied". */
    public CopyTargetActivatedOrTriggeredAbilityEffect thatCantBeCopied() {
        return new CopyTargetActivatedOrTriggeredAbilityEffect(copies, true);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.spellOnStack());
    }
}
