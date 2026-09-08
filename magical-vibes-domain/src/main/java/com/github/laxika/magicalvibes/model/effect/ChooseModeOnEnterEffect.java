package com.github.laxika.magicalvibes.model.effect;

import java.util.List;

/** "As this permanent enters, choose one of the listed named modes." */
public record ChooseModeOnEnterEffect(List<String> modes) implements CardEffect {
}
