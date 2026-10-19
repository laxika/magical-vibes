package com.github.laxika.magicalvibes.model.amount;

import com.github.laxika.magicalvibes.model.CardColor;

import java.util.Set;

/**
 * The number of effective colors of the spell that caused the current triggered ability that are
 * among the given chosen colors. The chosen colors are snapshotted when the trigger is put on the
 * stack, so the count survives the source leaving the battlefield; the spell's colors are read when
 * the ability resolves.
 */
public record TriggeringSpellChosenColorCount(Set<CardColor> chosenColors) implements DynamicAmount {

    public TriggeringSpellChosenColorCount {
        chosenColors = Set.copyOf(chosenColors);
    }
}
