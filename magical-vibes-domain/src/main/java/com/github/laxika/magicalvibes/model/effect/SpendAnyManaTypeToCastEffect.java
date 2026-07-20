package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;

import java.util.Set;

/**
 * Static marker effect: "You can spend mana of any type to cast [types] spells." While a permanent
 * with this effect is on the battlefield, its controller may pay the colored mana requirements of a
 * matching spell with mana of any type (including colorless). Used by Vizier of the Menagerie with
 * {@code CREATURE}.
 */
public record SpendAnyManaTypeToCastEffect(Set<CardType> spellTypes) implements AnyManaTypeCastEffect {
}
