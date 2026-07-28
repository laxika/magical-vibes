package com.github.laxika.magicalvibes.model.effect;

/**
 * When resolved, the effect's controller chooses which creatures block this combat and how those
 * creatures block, instead of the defending player. The defending player still owns the blocking
 * creatures and pays any block costs; only the declaration is handed over.
 *
 * <p>Registered by Melee. See {@code DelayedBlockerDeclarationControl}.
 */
public record ChooseBlockersThisCombatEffect() implements CardEffect {
}
