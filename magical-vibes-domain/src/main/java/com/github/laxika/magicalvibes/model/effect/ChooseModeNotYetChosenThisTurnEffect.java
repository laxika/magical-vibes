package com.github.laxika.magicalvibes.model.effect;

import java.util.List;

/**
 * A triggered modal ability whose modes can each be chosen once per turn.
 * The consumed labels are stored on the source permanent and cleared when a new turn begins.
 */
public record ChooseModeNotYetChosenThisTurnEffect(List<ChooseOneEffect.ChooseOneOption> options)
        implements CardEffect {
}
