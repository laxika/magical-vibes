package com.github.laxika.magicalvibes.model.effect;

/**
 * SPELL delayed-trigger registrar (Quenchable Fire). Reads the spell's already-chosen target — a
 * player or planeswalker carried in the stack entry's {@code targetId} — and schedules a delayed
 * trigger that fires at the beginning of the spell controller's next upkeep step: it deals
 * {@code damage} to that same player or planeswalker unless that player (or that planeswalker's
 * controller) pays {@code manaCost} before that step.
 *
 * <p>Piggybacks on the accompanying {@link DealDamageToTargetPlayerOrPlaneswalkerEffect}'s target and
 * declares no target of its own ({@link TargetSpec#NONE}); the handler queues a
 * {@code DamageAtNextUpkeepUnlessPays} delayed action, drained in {@code StepTriggerService}.
 *
 * @param damage   additional damage dealt at the controller's next upkeep if unpaid
 * @param manaCost mana the affected party may pay to avoid the damage (e.g. {@code "{U}"})
 */
public record RegisterDamageAtNextUpkeepUnlessPaysEffect(int damage, String manaCost) implements CardEffect {
}
