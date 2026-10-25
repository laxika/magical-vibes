package com.github.laxika.magicalvibes.model.effect;

/**
 * Mana-ability rider "When you spend this mana to cast a spell or activate an ability, copy that
 * spell or ability" (Sunken Palace). Every unit of mana the ability produces is tagged in the
 * {@code ManaPool}; the spell or non-mana ability a tagged unit actually pays for is copied once
 * per unit spent (CR 106.6a). Spending it on a mana ability or on anything else does nothing.
 */
public record RegisterNextSpellOrAbilityCopyEffect() implements CardEffect {
}
