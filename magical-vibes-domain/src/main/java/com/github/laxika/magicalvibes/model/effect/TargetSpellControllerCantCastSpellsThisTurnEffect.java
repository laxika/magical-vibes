package com.github.laxika.magicalvibes.model.effect;

/**
 * Spell effect: the controller of the targeted spell can't cast spells for the rest of the turn.
 * Used by Render Silent (DGM) alongside {@link CounterSpellEffect}; list this effect first so the
 * targeted spell is still on the stack when its controller is resolved.
 */
public record TargetSpellControllerCantCastSpellsThisTurnEffect() implements CardEffect {
}
