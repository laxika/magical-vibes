package com.github.laxika.magicalvibes.model.effect;

import java.util.List;

/** Prompts the stack entry's target player to choose one of the supplied modal options. */
public record TargetPlayerChoosesOneEffect(List<ChooseOneEffect.ChooseOneOption> options) implements CardEffect {

    public TargetPlayerChoosesOneEffect {
        options = List.copyOf(options);
    }
}
