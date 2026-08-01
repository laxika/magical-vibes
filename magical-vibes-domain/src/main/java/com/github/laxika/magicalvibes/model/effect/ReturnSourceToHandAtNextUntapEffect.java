package com.github.laxika.magicalvibes.model.effect;

/**
 * Delayed rider: "During your next untap step, as you untap your permanents, return this land to
 * its owner's hand" (Undiscovered Paradise).
 *
 * <p>Resolving this effect only schedules the bounce — it flags the source permanent
 * {@code returnToHandAtNextUntap}, and {@code UntapStepService} performs the actual return during
 * that permanent's controller's next untap step (even if the permanent itself does not untap). If
 * the permanent has left the battlefield by then nothing happens. On a mana ability the flag is
 * applied inline in {@code ActivatedAbilityExecutionService.doResolveManaAbility}, same rider
 * pattern as {@code PutCountersOnSelfEffect}.
 */
public record ReturnSourceToHandAtNextUntapEffect() implements CardEffect {
}
