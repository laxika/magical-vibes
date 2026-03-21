package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;

import java.util.Set;

/**
 * Static marker effect: "You may cast [types] from the top of your library."
 * While a permanent with this effect is on the battlefield, the controller may
 * cast spells of the specified types from the top of their library (paying their
 * mana cost normally).
 */
public record AllowCastFromTopOfLibraryEffect(Set<CardType> castableTypes) implements CardEffect {
}
