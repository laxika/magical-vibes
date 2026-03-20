package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.ManaColor;

/**
 * Triggered effect: adds mana equal to the number of attacking creatures the controller controls.
 * The player chooses one of the two offered colors; all mana is added as that color.
 * Also prevents the controller's mana pool from draining until end of turn.
 *
 * <p>Used by Grand Warlord Radha ("add that much mana in any combination of {R} and/or {G}.
 * Until end of turn, you don't lose this mana as steps and phases end.").
 */
public record AddManaPerAttackingCreatureEffect(ManaColor color1, ManaColor color2) implements CardEffect {
}
