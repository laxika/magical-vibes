package com.github.laxika.magicalvibes.model.effect;

import java.util.List;

/** Prompts the player carried by the stack entry to choose one of the supplied modal options. */
public record TargetPlayerChoosesOneEffect(List<ChooseOneEffect.ChooseOneOption> options, boolean targetsPlayer) implements CardEffect {

    public TargetPlayerChoosesOneEffect(List<ChooseOneEffect.ChooseOneOption> options) {
        this(options, false);
    }

    public static TargetPlayerChoosesOneEffect forTargetedPlayer(List<ChooseOneEffect.ChooseOneOption> options) {
        return new TargetPlayerChoosesOneEffect(options, true);
    }

    public TargetPlayerChoosesOneEffect {
        options = List.copyOf(options);
    }

    @Override
    public TargetSpec targetSpec() {
        return targetsPlayer ? TargetSpec.benign(TargetPredicates.player()) : TargetSpec.NONE;
    }
}
