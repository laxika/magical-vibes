package com.github.laxika.magicalvibes.model.effect;

import java.util.List;

/** "As this permanent enters, choose one or more of the listed named modes." */
public record ChooseModeOnEnterEffect(List<String> modes, boolean eachPlayer, int choicesRequired)
        implements CardEffect {

    public ChooseModeOnEnterEffect(List<String> modes) {
        this(modes, false, 1);
    }

    public ChooseModeOnEnterEffect(List<String> modes, boolean eachPlayer) {
        this(modes, eachPlayer, 1);
    }

    public ChooseModeOnEnterEffect(List<String> modes, int choicesRequired) {
        this(modes, false, choicesRequired);
    }

    public ChooseModeOnEnterEffect {
        modes = List.copyOf(modes);
        if (modes.isEmpty()) {
            throw new IllegalArgumentException("At least one mode is required");
        }
        if (choicesRequired < 1 || choicesRequired > modes.stream().distinct().count()) {
            throw new IllegalArgumentException(
                    "The required choice count must be between one and the number of different modes");
        }
        if (eachPlayer && choicesRequired != 1) {
            throw new IllegalArgumentException("Each-player mode choices support exactly one mode");
        }
    }
}
