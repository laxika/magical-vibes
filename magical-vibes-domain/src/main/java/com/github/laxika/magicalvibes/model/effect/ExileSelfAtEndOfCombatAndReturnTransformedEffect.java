package com.github.laxika.magicalvibes.model.effect;

/**
 * When this permanent attacks, exile it at end of combat, then return it to the battlefield
 * transformed under its controller's control. Used on ON_ATTACK effect slot.
 *
 * <p>Resolution adds the source permanent ID to
 * {@code GameData.pendingExileAndReturnTransformedAtEndOfCombat}. At end of combat,
 * the permanent is exiled and immediately returned as its back face.</p>
 */
public record ExileSelfAtEndOfCombatAndReturnTransformedEffect() implements CardEffect {
}
