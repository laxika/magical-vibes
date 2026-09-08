package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.PowerToughnessForm;

import java.util.List;

/**
 * Entry/face-up marker for a permanent that becomes one of a set of chosen base P/T forms.
 * The choice is handled by the entry and morph turn-up interaction pipelines.
 */
public record ChoosePowerToughnessFormEffect(List<PowerToughnessForm> forms)
        implements PowerToughnessFormChoiceEffect {

    public ChoosePowerToughnessFormEffect {
        forms = List.copyOf(forms);
    }
}
