package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.StackEntry;

/** Abandons this scheme, then sets the scheme that caused this trigger in motion again. */
public record AbandonSchemeAndSetTriggeringSchemeInMotionAgainEffect(StackEntry schemeSnapshot)
        implements CardEffect {

    public AbandonSchemeAndSetTriggeringSchemeInMotionAgainEffect() {
        this(null);
    }

    @Override
    public TargetSpec targetSpec() {
        return new TargetSpec(null, false, null, true, 1);
    }
}
