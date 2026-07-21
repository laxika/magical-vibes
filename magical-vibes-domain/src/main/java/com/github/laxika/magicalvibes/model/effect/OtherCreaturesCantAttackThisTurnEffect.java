package com.github.laxika.magicalvibes.model.effect;

/**
 * Intimidation Bolt's rider: "Other creatures can't attack this turn." A one-shot {@code SPELL}-slot
 * effect that installs a turn-scoped attack lock exempting only the creature the spell targeted (read
 * from the stack entry at resolution). Resolved by
 * {@code OtherCreaturesCantAttackThisTurnEffectHandler}, which appends the targeted creature's ID to
 * {@code GameData.otherCreaturesCantAttackExemptCreatureIds}; the lock is enforced in
 * {@code CombatAttackService.canCreatureAttack} and covers creatures that enter the battlefield later
 * this turn (a continuous restriction), not just those present when the spell resolved. Untargeted
 * itself — it reads the accompanying damage effect's target — so {@code targetSpec()} stays
 * {@code NONE}.
 */
public record OtherCreaturesCantAttackThisTurnEffect() implements CardEffect {
}
