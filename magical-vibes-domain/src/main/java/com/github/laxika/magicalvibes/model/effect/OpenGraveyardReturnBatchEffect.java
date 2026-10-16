package com.github.laxika.magicalvibes.model.effect;

/**
 * Starts collecting the graveyard cards that the following effects choose to put onto the battlefield, so
 * they all enter together when a {@link CloseGraveyardReturnBatchEffect} resolves (Liliana, the Necromancer's
 * "put up to two creature cards from graveyards onto the battlefield").
 */
public record OpenGraveyardReturnBatchEffect() implements CardEffect {
}
