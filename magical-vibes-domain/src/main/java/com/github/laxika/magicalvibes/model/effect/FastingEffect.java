package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.DrawReplacementKind;

/** Marker in the draw-step replacement slot for Fasting's optional whole-step skip. */
public record FastingEffect() implements MaySkipDrawReplacementEffect {

    @Override
    public DrawReplacementKind replacementKind() {
        return DrawReplacementKind.FASTING;
    }
}
