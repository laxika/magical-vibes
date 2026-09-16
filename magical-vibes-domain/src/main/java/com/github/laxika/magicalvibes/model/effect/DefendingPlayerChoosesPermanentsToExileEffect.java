package com.github.laxika.magicalvibes.model.effect;

/**
 * During an attack trigger, the defending player chooses the specified number of permanents they
 * control to exile, or all of their permanents if fewer are available.
 */
public record DefendingPlayerChoosesPermanentsToExileEffect(int count) implements CardEffect {
}
