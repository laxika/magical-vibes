package com.github.laxika.magicalvibes.model.effect;

import java.util.List;

/**
 * Attack-triggered modal whose selected mode is unavailable during the next combat.
 *
 * <p>The selected label is remembered on the source permanent and is replaced when the next
 * trigger resolves, so a permanent that survives combats alternates between its modes.</p>
 */
public record ChooseModeNotChosenDuringLastCombatEffect(
        List<ChooseOneEffect.ChooseOneOption> options) implements CardEffect {
}
