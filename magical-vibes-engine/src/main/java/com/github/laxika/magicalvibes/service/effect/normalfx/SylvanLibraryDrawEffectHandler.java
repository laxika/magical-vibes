package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SylvanLibraryDrawEffect;
import com.github.laxika.magicalvibes.service.DrawService;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link SylvanLibraryDrawEffect}: the controller draws two additional cards, then (via
 * {@link PendingInteraction.SylvanLibraryChoice}) chooses two cards in their hand drawn this turn;
 * for each they pay 4 life or put it on top of their library.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SylvanLibraryDrawEffectHandler implements NormalEffectHandlerBean {

    private final DrawService drawService;
    private final GameBroadcastService gameBroadcastService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SylvanLibraryDrawEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        String controllerName = gameData.playerIdToName.get(controllerId);
        String sourceName = entry.getCard().getName();

        // Draw two additional cards.
        drawService.resolveDrawCard(gameData, controllerId);
        drawService.resolveDrawCard(gameData, controllerId);
        gameBroadcastService.logAndBroadcast(gameData,
                GameLog.text(controllerName + " draws two additional cards (" + sourceName + ")."));

        // Determine which cards still in hand were drawn this turn (hand order preserved).
        List<UUID> drawnThisTurn = gameData.cardsDrawnThisTurnIds.getOrDefault(controllerId, List.of());
        List<Card> hand = gameData.playerHands.get(controllerId);
        List<UUID> pool = new ArrayList<>();
        if (hand != null) {
            for (Card card : hand) {
                if (drawnThisTurn.contains(card.getId()) && !pool.contains(card.getId())) {
                    pool.add(card.getId());
                }
            }
        }

        int resolveCount = Math.min(2, pool.size());
        if (resolveCount == 0) {
            log.info("Game {} - {} has no cards drawn this turn in hand ({})", gameData.id, controllerName, sourceName);
            return;
        }

        interactionHandlerRegistry.begin(gameData,
                new PendingInteraction.SylvanLibraryChoice(controllerId, pool, resolveCount));

        log.info("Game {} - Awaiting {} to resolve {} card(s) for {} (pool of {})",
                gameData.id, controllerName, resolveCount, sourceName, pool.size());
    }
}
