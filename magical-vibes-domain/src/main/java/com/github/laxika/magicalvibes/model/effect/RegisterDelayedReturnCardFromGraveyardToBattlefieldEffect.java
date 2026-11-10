package com.github.laxika.magicalvibes.model.effect;

/**
 * Registers a delayed trigger that returns a card from its owner's graveyard to the battlefield under the ability
 * controller's control at the beginning of the next end step.
 *
 * <p>The card is the stack entry's triggering card ("that card"), in the graveyard incarnation the ability recorded
 * (CR 400.7); which cards trigger it ("a Desert card") is the ability's trigger filter.
 */
public record RegisterDelayedReturnCardFromGraveyardToBattlefieldEffect() implements CardEffect {
}
