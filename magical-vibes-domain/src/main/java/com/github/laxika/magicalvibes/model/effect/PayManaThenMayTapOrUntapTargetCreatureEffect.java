package com.github.laxika.magicalvibes.model.effect;

/**
 * On resolution, the controller may pay {@code manaCost}. When they do, a reflexive triggered
 * ability is put on the stack; that ability may tap or untap target creature.
 */
public record PayManaThenMayTapOrUntapTargetCreatureEffect(String manaCost) implements CardEffect {
}
