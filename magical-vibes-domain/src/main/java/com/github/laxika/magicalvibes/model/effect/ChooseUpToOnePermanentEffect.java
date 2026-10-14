package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * "[Effect] on up to one [permanent]" without the word "target": the controller chooses up to one
 * permanent matching {@code filter} on any battlefield as this effect resolves, not when the spell
 * or ability is put on the stack. The choice is recorded as the stack entry's chosen permanent
 * (cleared when nothing is chosen), so later steps of the same resolution act on it through
 * {@link PutCountersOnChosenPermanentEffect} and {@link GrantKeywordToChosenCreatureEffect}. The
 * resolution pauses for the choice and continues with the next step once it is answered.
 */
public record ChooseUpToOnePermanentEffect(PermanentPredicate filter, String prompt) implements CardEffect {
}
