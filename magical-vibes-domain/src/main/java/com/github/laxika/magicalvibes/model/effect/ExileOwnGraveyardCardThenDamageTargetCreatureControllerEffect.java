package com.github.laxika.magicalvibes.model.effect;

/**
 * Resolution-time "if you do" bundle: exile a card from the controller's graveyard, and if a card
 * is actually exiled, deal {@code damage} to the target creature's controller. Intended to be
 * wrapped in a {@link MayEffect} so the exile is optional ("You may exile a card from your
 * graveyard. If you do, ... deals N damage to that creature's controller."). If the controller's
 * graveyard is empty, nothing happens and no damage is dealt. Used by Heated Argument.
 */
public record ExileOwnGraveyardCardThenDamageTargetCreatureControllerEffect(int damage) implements CardEffect {

    @Override
    public boolean canTargetPermanent() {
        return true;
    }
}
