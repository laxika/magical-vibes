package com.github.laxika.magicalvibes.model.effect;

/**
 * Relic Bind's modal triggered ability: "Whenever enchanted artifact becomes tapped, choose one —
 * this Aura deals 1 damage to target player or planeswalker; or target player gains 1 life."
 * <p>
 * Placed in the {@code ON_ENCHANTED_PERMANENT_TAPPED} slot. The trigger collector translates this
 * marker into the shared triggered-modal queue: the controller chooses its mode and target before
 * any player receives priority. The chosen damage or life-gain effect resolves normally.
 */
public record RelicBindTapEffect() implements CardEffect {
}
