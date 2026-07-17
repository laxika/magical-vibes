package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Shared logic for Ad Nauseam's repeatable "reveal the top card, put it into hand, lose life
 * equal to its mana value" process. Used by both {@link AdNauseamEffectHandler} (the mandatory
 * first iteration) and the repeat-choice interaction handler (each accepted repeat).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AdNauseamSupport {

    private final LifeSupport lifeSupport;
    private final GameBroadcastService gameBroadcastService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    /**
     * Performs one iteration of the process. Assumes the controller's library is non-empty.
     * Reveals the top card, puts it into hand, and (when its mana value is greater than zero)
     * makes the controller lose that much life.
     */
    public void revealTopCardAndLoseLife(GameData gameData, UUID controllerId, String sourceName) {
        List<Card> deck = gameData.playerDecks.get(controllerId);
        String playerName = gameData.playerIdToName.get(controllerId);

        Card topCard = deck.removeFirst();
        int manaValue = topCard.getManaValue();

        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(playerName + " reveals " + topCard.getName()
                + " (mana value " + manaValue + ") from the top of their library."));
        gameData.addCardToHand(controllerId, topCard);

        if (manaValue > 0) {
            lifeSupport.applyLifeLoss(gameData, controllerId, manaValue, sourceName);
        } else {
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(playerName + " puts " + topCard.getName()
                    + " into their hand (" + sourceName + ")."));
        }

        log.info("Game {} - {} reveals {} (MV {}) via {}", gameData.id, playerName, topCard.getName(), manaValue, sourceName);
    }

    /**
     * Begins the repeat accept/decline prompt for {@code controllerId} when their library still
     * has cards to reveal. Returns {@code true} when a prompt was begun (resolution must pause),
     * {@code false} when the library is empty and the process is over.
     */
    public boolean beginRepeatPromptIfPossible(GameData gameData, UUID controllerId, String sourceName) {
        List<Card> deck = gameData.playerDecks.get(controllerId);
        if (deck == null || deck.isEmpty()) {
            return false;
        }
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.AdNauseamRepeatChoice(controllerId, sourceName));
        return true;
    }
}
