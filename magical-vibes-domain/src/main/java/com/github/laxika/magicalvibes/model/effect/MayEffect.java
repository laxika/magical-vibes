package com.github.laxika.magicalvibes.model.effect;

public record MayEffect(CardEffect wrapped, String prompt, boolean triggersPerBlocker) implements CardEffect {

    public MayEffect(CardEffect wrapped, String prompt) {
        this(wrapped, prompt, false);
    }

    @Override
    public boolean triggersPerBlocker() { return triggersPerBlocker; }
}
