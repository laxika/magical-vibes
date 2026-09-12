package com.github.laxika.magicalvibes.model.effect;

import java.util.List;

/** "As this permanent enters, choose one of the listed named modes." */
public record ChooseModeOnEnterEffect(List<String> modes, boolean eachPlayer) implements CardEffect {

    public ChooseModeOnEnterEffect(List<String> modes) {
        this(modes, false);
    }
}
