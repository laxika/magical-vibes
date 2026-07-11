package com.github.laxika.magicalvibes.model.effect;

/**
 * Look at target player's hand. The caster may choose a nonland card from it; if they do, that
 * player reveals the chosen card, puts it on the bottom of their library, then draws a card.
 * The choice is optional (the caster may decline even when a legal card exists). Used by
 * Vendilion Clique ({@code ON_ENTER_BATTLEFIELD}, player target).
 */
public record LookAtHandChooseNonlandToBottomAndDrawEffect() implements CardEffect {
    @Override
    public boolean canTargetPlayer() {
        return true;
    }
}
