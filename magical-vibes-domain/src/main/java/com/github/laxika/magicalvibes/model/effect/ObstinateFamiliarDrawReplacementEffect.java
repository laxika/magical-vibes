package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.DrawReplacementKind;

/**
 * Static marker for Obstinate Familiar: its controller may skip each card they would draw.
 */
public record ObstinateFamiliarDrawReplacementEffect() implements MaySkipDrawReplacementEffect {

    @Override
    public DrawReplacementKind replacementKind() {
        return DrawReplacementKind.OBSTINATE_FAMILIAR;
    }
}
