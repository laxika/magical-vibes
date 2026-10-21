package com.github.laxika.magicalvibes.ai.simulation;

import com.github.laxika.magicalvibes.carddata.CardRegistry;
import com.github.laxika.magicalvibes.carddata.DeckLegalityRegistry;
import com.github.laxika.magicalvibes.cards.RandomDeckGenerator;
import org.springframework.stereotype.Component;

/**
 * Hands the application's card-data beans to {@link HeadlessSimulationContext} at startup, before
 * any hard-AI game can build it, so the simulation context reuses the loaded sets instead of loading
 * them all again.
 */
@Component
class HeadlessSimulationCardDataSharing {

    HeadlessSimulationCardDataSharing(CardRegistry cardRegistry, RandomDeckGenerator randomDeckGenerator,
                                      DeckLegalityRegistry deckLegalityRegistry) {
        HeadlessSimulationContext.shareCardData(cardRegistry, randomDeckGenerator, deckLegalityRegistry);
    }
}
