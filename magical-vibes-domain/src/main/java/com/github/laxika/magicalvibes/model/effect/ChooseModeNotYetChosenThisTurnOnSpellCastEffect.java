package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

import java.util.List;

/**
 * Trigger descriptor for a controller's spell-cast ability that offers one mode not already
 * chosen this turn. The trigger collector records the selected mode on the source permanent.
 */
public record ChooseModeNotYetChosenThisTurnOnSpellCastEffect(
        CardPredicate spellFilter,
        List<ChooseOneEffect.ChooseOneOption> options
) implements CardEffect {

    public ChooseModeNotYetChosenThisTurnOnSpellCastEffect {
        options = List.copyOf(options);
    }
}
