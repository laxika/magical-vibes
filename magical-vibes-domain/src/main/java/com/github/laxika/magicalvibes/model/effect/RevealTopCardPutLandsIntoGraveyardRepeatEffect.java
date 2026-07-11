package com.github.laxika.magicalvibes.model.effect;

/**
 * "Reveal the top card of your library. If it's a land card, put it into your graveyard and repeat
 * this process." Reveals the controller's library one card at a time, putting each revealed land card
 * into the graveyard, stopping at the first non-land card (which stays on top) or an empty library.
 * Used by Countryside Crusher.
 */
public record RevealTopCardPutLandsIntoGraveyardRepeatEffect() implements CardEffect {
}
