package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.ManaColor;

/**
 * Schedules adding a fixed amount of mana of {@code color} at the beginning of the controller's
 * next main phase this turn (mandatory — not a "you may"). Used by Conduit of Storms / Conduit of
 * Emrakul's attack triggers. Fires whether or not the source is still on the battlefield.
 *
 * @param color  mana color to add ({@code RED} or {@code COLORLESS})
 * @param amount how much mana to add
 */
public record RegisterDelayedManaAtNextMainPhaseEffect(ManaColor color, int amount) implements CardEffect {
}
