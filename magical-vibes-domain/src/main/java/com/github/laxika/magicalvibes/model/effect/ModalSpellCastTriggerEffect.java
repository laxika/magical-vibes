package com.github.laxika.magicalvibes.model.effect;

import java.util.List;

/**
 * Trigger descriptor for an ability that triggers only when its controller casts a modal spell.
 * The trigger collector uses the number of modes chosen for that spell to build the resolution-time
 * choice for the ability.
 */
public record ModalSpellCastTriggerEffect(List<ChooseOneEffect.ChooseOneOption> options) implements CardEffect {

    public ModalSpellCastTriggerEffect {
        options = List.copyOf(options);
    }
}
