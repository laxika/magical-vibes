package com.github.laxika.magicalvibes.model.effect;

/** Marks the source permanent as motivated by the player who paid its motivate cost. A special action: it creates no stack object. */
public record MotivateEffect() implements CardEffect {
    @Override
    public boolean isSpecialAction() { return true; }
}
