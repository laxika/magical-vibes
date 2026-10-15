package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.ManaColor;

/**
 * Triggered effect: adds mana equal to the number of attacking creatures the controller controls.
 * Each mana's color is chosen separately from the two offered colors, and that mana doesn't empty
 * from the controller's pool until end of turn.
 *
 * <p>Used by Grand Warlord Radha ("add that much mana in any combination of {R} and/or {G}.
 * Until end of turn, you don't lose this mana as steps and phases end.").
 */
public record AddManaPerAttackingCreatureEffect(ManaColor color1, ManaColor color2) implements CardEffect {
}
