package com.github.laxika.magicalvibes.model.effect;

/**
 * Replacement effect: each creature (any controller) whose mana value does NOT match the source
 * permanent's chosen odd/even quality enters the battlefield tapped. The chosen parity is read from
 * the source permanent at runtime via {@code Permanent.getChosenManaValueParity()}; while unchosen
 * (null), no creature is tapped. Handled during permanent entry, not via the trigger pipeline.
 * Used by Ashling's Prerogative ("Each creature without mana value of the chosen quality enters tapped.").
 */
public record CreaturesOfUnchosenParityEnterTappedEffect() implements CardEffect {
}
