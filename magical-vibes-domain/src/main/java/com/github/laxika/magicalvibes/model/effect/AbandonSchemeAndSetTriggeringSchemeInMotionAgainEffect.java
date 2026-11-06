package com.github.laxika.magicalvibes.model.effect;

/**
 * Abandons this scheme, then sets the scheme that caused this trigger in motion again: the stack entry's triggering
 * spell, which holds that scheme's set-in-motion stack object as it was when this ability triggered.
 */
public record AbandonSchemeAndSetTriggeringSchemeInMotionAgainEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return new TargetSpec(null, false, null, true, 1);
    }
}
