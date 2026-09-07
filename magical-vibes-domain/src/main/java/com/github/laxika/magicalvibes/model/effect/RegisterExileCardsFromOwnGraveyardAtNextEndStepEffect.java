package com.github.laxika.magicalvibes.model.effect;

/** Registers a delayed trigger that exiles cards from the controller's graveyard at the next end step. */
public record RegisterExileCardsFromOwnGraveyardAtNextEndStepEffect(int count) implements CardEffect {
}
