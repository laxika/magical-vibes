package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

/**
 * Each player chooses a creature they control, puts the supplied counter on it, then sacrifices
 * all other creatures they control. The chosen creatures also receive a permanent attack
 * restriction tied to the counter.
 */
public record EachPlayerChoosesCreatureThenSacrificesRestEffect(CounterType counterType)
        implements CardEffect {
}
