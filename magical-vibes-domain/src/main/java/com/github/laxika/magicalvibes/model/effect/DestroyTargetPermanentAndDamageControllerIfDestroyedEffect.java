package com.github.laxika.magicalvibes.model.effect;

/**
 * Destroys the targeted permanent and, only if that permanent is actually put into a graveyard
 * as a result, deals {@code damage} noncombat damage to that permanent's controller.
 * Used by Werewolf Ransacker and similar cards with "If that artifact is put into a graveyard
 * this way, deal N damage to that artifact's controller."
 */
public record DestroyTargetPermanentAndDamageControllerIfDestroyedEffect(int damage) implements CardEffect {

    @Override
    public boolean canTargetPermanent() {
        return true;
    }

    @Override
    public boolean isDamageOrDestruction() {
        return true;
    }
}
