package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Static effect: each spell the controller casts that matches {@code filter} has conspire (CR 702.78),
 * i.e. as it is cast the controller may tap two untapped creatures sharing a colour with it to copy it.
 * Consulted by the conspire-cost gate in the spell-casting flow alongside the innate {@code CONSPIRE}
 * keyword. Used by Wort, the Raidmother (red or green instant or sorcery spells).
 */
public record GrantConspireToSpellsEffect(CardPredicate filter) implements CardEffect {
}
