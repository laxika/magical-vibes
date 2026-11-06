package com.github.laxika.magicalvibes.model.effect;

/**
 * "Copy that spell or ability. You may choose new targets for the copy." (Bill Potts): copies the spell or the
 * activated ability the trigger event was about, read from the stack entry. For a cast it copies the triggering spell
 * as {@link CopyTriggeringSpellEffect} does, the copy controlled by that spell's controller; for an activation it
 * copies the activated ability ({@code StackEntry.getTriggeringActivatedAbility()}, its stack entry as the triggering
 * spell snapshot), the copy controlled by this ability's controller. Either way the copy's controller may choose new
 * targets for it (CR 707.10c).
 * <p>
 * An ability with an activation trigger and this effect is collected once the activated ability is on the stack, so
 * the copy has its targets and X; the activation trigger's stack-entry filter
 * ({@code AbilityTrigger.withStackEntryFilter}) is judged then.
 */
public record CopyTriggeringSpellOrAbilityEffect() implements TriggeringSpellReferencingEffect {
}
