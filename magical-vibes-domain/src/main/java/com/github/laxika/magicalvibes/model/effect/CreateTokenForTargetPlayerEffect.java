package com.github.laxika.magicalvibes.model.effect;

/**
 * Creates tokens under the targeted player's control (not the controller's).
 * Used by cards like Dowsing Dagger that say "target opponent creates [tokens]".
 */
public record CreateTokenForTargetPlayerEffect(CreateTokenEffect tokenEffect) implements CardEffect {
    @Override
    public boolean canTargetPlayer() {
        return true;
    }
}
