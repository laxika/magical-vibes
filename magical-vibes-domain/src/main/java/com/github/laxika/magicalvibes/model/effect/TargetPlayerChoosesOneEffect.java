package com.github.laxika.magicalvibes.model.effect;

import java.util.List;

/**
 * Prompts the player carried by the stack entry to choose one of the supplied modal options. When
 * {@code villainousChoice} is set, the choice is "faces a villainous choice" so replacement effects
 * that repeat it (The Valeyard) apply.
 */
public record TargetPlayerChoosesOneEffect(List<ChooseOneEffect.ChooseOneOption> options, boolean targetsPlayer,
                                           boolean villainousChoice)
        implements CombatDamageTriggerContextEffect {

    public TargetPlayerChoosesOneEffect(List<ChooseOneEffect.ChooseOneOption> options) {
        this(options, false, false);
    }

    public static TargetPlayerChoosesOneEffect forTargetedPlayer(List<ChooseOneEffect.ChooseOneOption> options) {
        return new TargetPlayerChoosesOneEffect(options, true, false);
    }

    /** The carried (e.g. damaged) player faces a villainous choice between the supplied options. */
    public static TargetPlayerChoosesOneEffect forTargetedPlayerVillainousChoice(
            List<ChooseOneEffect.ChooseOneOption> options) {
        return new TargetPlayerChoosesOneEffect(options, true, true);
    }

    public TargetPlayerChoosesOneEffect {
        options = List.copyOf(options);
    }

    @Override
    public TargetSpec targetSpec() {
        return targetsPlayer ? TargetSpec.benign(TargetPredicates.player()) : TargetSpec.NONE;
    }

    @Override
    public TriggerContext combatDamageTriggerContext() {
        return targetsPlayer ? TriggerContext.DAMAGED_PLAYER : null;
    }
}
