package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.LibraryOwner;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScryEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry interactionHandlerRegistry;
    private final AmountEvaluationService amountEvaluationService;
    private final GameQueryService gameQueryService;
    private final TriggerCollectionService triggerCollectionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ScryEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        ScryEffect e = (ScryEffect) effect;

        UUID controllerId = entry.getControllerId();
        boolean targetLibrary = e.owner() == LibraryOwner.TARGET_PLAYER;
        UUID libraryOwnerId = e.owner() == LibraryOwner.TARGET_PLAYER && entry.getTargetId() != null
                ? entry.getTargetId()
                : controllerId;
        List<Card> deck = gameData.playerDecks.get(libraryOwnerId);

        Permanent source = entry.getSourcePermanentId() != null
                ? gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId())
                : null;
        if (source == null) {
            source = entry.getSourcePermanentSnapshot();
        }
        int scryAmount = Math.max(0, amountEvaluationService.evaluate(gameData, e.count(),
                AmountContext.forStackEntry(entry, source)));

        // 701.22b: If a player is instructed to scry 0, no scry event occurs.
        if (scryAmount == 0) {
            return;
        }

        int count = Math.min(scryAmount, deck.size());

        // 701.22d: Empty library — scry event still occurs (triggers would fire), but nothing to interact with.
        if (count == 0) {
            String logMsg = targetLibrary
                    ? gameData.playerIdToName.get(controllerId) + " looks at the top " + scryAmount
                            + " cards of " + libraryName(gameData, controllerId, libraryOwnerId)
                            + ", but it is empty."
                    : gameData.playerIdToName.get(controllerId) + " scries " + scryAmount
                            + " but their library is empty.";
            gameLogService.append(gameData, GameLog.text(logMsg));
            if (!targetLibrary) {
                triggerCollectionService.checkScryTriggers(gameData, controllerId, 0);
            }
            return;
        }

        List<Card> topCards = new ArrayList<>(deck.subList(0, count));
        deck.subList(0, count).clear();

        interactionHandlerRegistry.begin(gameData,
                new PendingInteraction.Scry(controllerId, topCards, false, libraryOwnerId));

        String logMsg = targetLibrary
                ? gameData.playerIdToName.get(controllerId) + " looks at the top " + count
                        + " cards of " + libraryName(gameData, controllerId, libraryOwnerId) + "."
                : gameData.playerIdToName.get(controllerId) + " scries " + count + ".";
        gameLogService.append(gameData, GameLog.text(logMsg));
        if (targetLibrary) {
            log.info("Game {} - {} looks at {} cards of {}", gameData.id,
                    gameData.playerIdToName.get(controllerId), count,
                    libraryName(gameData, controllerId, libraryOwnerId));
        } else {
            log.info("Game {} - {} scries {}", gameData.id, gameData.playerIdToName.get(controllerId), count);
        }
    }

    private static String libraryName(GameData gameData, UUID controllerId, UUID libraryOwnerId) {
        return controllerId.equals(libraryOwnerId)
                ? "their library"
                : gameData.playerIdToName.get(libraryOwnerId) + "'s library";
    }
}
