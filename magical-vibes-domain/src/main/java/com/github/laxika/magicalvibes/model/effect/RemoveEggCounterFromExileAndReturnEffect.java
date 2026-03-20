package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/**
 * Triggered-ability effect for cards exiled with egg counters (e.g. Darigaaz Reincarnated).
 *
 * <p>"At the beginning of your upkeep, if this card is exiled with an egg counter on it,
 * remove an egg counter from it. Then if this card has no egg counters on it, return it
 * to the battlefield."</p>
 *
 * <p>Resolution is dispatched to {@code ExileEggCounterResolutionService} via
 * {@code @HandlesEffect}. The {@code cardId} identifies which exiled card to process.</p>
 */
public record RemoveEggCounterFromExileAndReturnEffect(UUID cardId) implements CardEffect {
}
