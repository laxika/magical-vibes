package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import java.util.Set;

/**
 * Non-targeting-by-itself ability effect: the player carried as the stack entry's target
 * can't cast spells of {@code restrictedTypes} for the rest of this turn. Cleared at end of turn.
 * Used by Moonhold ({@code CREATURE}, when {@code W} was spent to cast it) and
 * Abeyance ({@code INSTANT}, {@code SORCERY}).
 */
public record TargetPlayerCantCastSpellTypesThisTurnEffect(Set<CardType> restrictedTypes) implements CardEffect {
}
