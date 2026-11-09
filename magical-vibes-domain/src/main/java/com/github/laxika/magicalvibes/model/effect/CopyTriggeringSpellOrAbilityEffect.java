package com.github.laxika.magicalvibes.model.effect;

/**
 * "Copy that spell or ability. You may choose new targets for the copy." (Bill Potts), and "copy that ability" of the
 * activation triggers (Rings of Brighthearth, Illusionist's Bracers, Verrak, Warped Sengir): copies the spell or the
 * activated ability the trigger event was about, read from the stack entry. For a cast it copies the triggering spell
 * as {@link CopyTriggeringSpellEffect} does, the copy controlled by that spell's controller; for an activation it
 * copies the activated ability ({@code StackEntry.getTriggeringActivatedAbility()}, its stack entry as the triggering
 * spell snapshot), the copy controlled by this ability's controller. Either way the copy's controller may choose new
 * targets for it (CR 707.10c).
 * <p>
 * An ability with an activation trigger and this effect, alone or behind a "you may" ({@link MayEffect}), a "you may
 * pay" ({@link MayPayManaEffect}, {@link MayPayLifeEqualToAmountEffect} with the life paid to activate it as the
 * entry's event value) or an "if", is collected once the activated ability is on the stack, so the copy has its
 * targets and X; the activation trigger's stack-entry and payment filters ({@code AbilityTrigger.withStackEntryFilter},
 * {@code withPayment}) are judged then. A "you may" is asked as the ability resolves (CR 603.5).
 */
public record CopyTriggeringSpellOrAbilityEffect() implements CardEffect {
}
