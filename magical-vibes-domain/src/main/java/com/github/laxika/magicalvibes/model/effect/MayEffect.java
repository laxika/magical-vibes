package com.github.laxika.magicalvibes.model.effect;

/**
 * "You may [wrapped]" — the choice is made by the controller at resolution time (CR 603.5).
 *
 * @param wrapped    the effect resolved when the controller accepts
 * @param prompt     the accept/decline prompt text
 * @param elseEffect optional "if you don't, [effect]" half resolved when the controller declines
 *                   (Petals of Insight's "Otherwise, draw three cards"); {@code null} means
 *                   declining simply does nothing
 */
public record MayEffect(CardEffect wrapped, String prompt, CardEffect elseEffect) implements CardEffect {

    /** Plain "you may" with nothing happening on a decline. */
    public MayEffect(CardEffect wrapped, String prompt) {
        this(wrapped, prompt, null);
    }

    @Override
    public TargetSpec targetSpec() {
        return wrapped.targetSpec();
    }
}
