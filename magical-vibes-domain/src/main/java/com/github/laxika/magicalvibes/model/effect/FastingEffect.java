package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.DrawReplacementKind;

/** Fasting's static ability: "If you would begin your draw step, you may skip that step instead. If you do, you gain 2 life." */
public record FastingEffect() implements MaySkipDrawReplacementEffect {

    @Override
    public DrawReplacementKind replacementKind() {
        return DrawReplacementKind.FASTING;
    }

    @Override
    public boolean drawStepOnly() {
        return true;
    }
}
