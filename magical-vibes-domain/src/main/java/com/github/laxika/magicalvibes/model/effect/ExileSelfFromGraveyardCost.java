package com.github.laxika.magicalvibes.model.effect;

/**
 * Cost effect for a graveyard activated ability that requires exiling its own source card from
 * the controller's graveyard (e.g. Embalm / Eternalize: "Exile this card from your graveyard").
 *
 * <p>Paid at activation time in {@code AbilityActivationService.activateGraveyardAbilityImpl}
 * (like {@link ExileNCardsFromGraveyardCost}), removing the source card from the graveyard and
 * putting it into exile before the ability goes on the stack — so the ability can't be activated
 * twice off the same graveyard card.
 */
public record ExileSelfFromGraveyardCost() implements CostEffect {
}
