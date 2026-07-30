package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Shared logic for Primal Surge's "exile the top card; if it's a permanent card you may put it
 * onto the battlefield; if you do, repeat this process" loop. Used by
 * {@link ExileTopCardMayPutPermanentOntoBattlefieldRepeatEffectHandler} (the first iteration) and
 * by the put-choice interaction handler (each accepted put).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExileTopCardMayPutPermanentSupport {

    private final ExileService exileService;
    private final GameLogService gameLogService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    /**
     * Runs one iteration: exiles the top card of {@code controllerId}'s library and, when it is a
     * permanent card, begins the accept/decline put prompt. Returns {@code true} when a prompt was
     * begun (resolution must pause), {@code false} when the process is over — an empty library, a
     * nonpermanent card (it stays in exile), or an ETB from a previously put permanent that already
     * opened its own interaction.
     */
    public boolean exileTopCardAndPromptIfPermanent(GameData gameData, UUID controllerId, String sourceName) {
        if (gameData.interaction.isAwaitingInput()) {
            // A previously put permanent's ETB opened its own prompt; that decision owns the
            // interaction slot, so the loop stops here rather than clobbering it.
            return false;
        }

        List<Card> deck = gameData.playerDecks.get(controllerId);
        String playerName = gameData.playerIdToName.get(controllerId);

        if (deck == null || deck.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(playerName + "'s library is empty (" + sourceName + ")."));
            return false;
        }

        Card topCard = deck.removeFirst();
        exileService.exileCard(gameData, controllerId, topCard);
        gameLogService.append(gameData, GameLog.textCardText(
                playerName + " exiles ", topCard, " from the top of their library (" + sourceName + ")."));

        if (!topCard.getType().isPermanentType()) {
            gameLogService.append(gameData, GameLog.cardThen(topCard, " isn't a permanent card; it stays in exile."));
            log.info("Game {} - {} ends on nonpermanent {}", gameData.id, sourceName, topCard.getName());
            return false;
        }

        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ExiledPermanentPutOntoBattlefieldChoice(
                controllerId, sourceName, topCard.getId(), topCard.getName()));
        return true;
    }
}
