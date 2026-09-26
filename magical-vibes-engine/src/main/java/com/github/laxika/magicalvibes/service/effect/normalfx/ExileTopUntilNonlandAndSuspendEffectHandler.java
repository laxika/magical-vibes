package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopUntilNonlandAndSuspendEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** Resolves a library dig that gives the first nonland card suspend. */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExileTopUntilNonlandAndSuspendEffectHandler implements NormalEffectHandlerBean {

    private final ExileService exileService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTopUntilNonlandAndSuspendEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        ExileTopUntilNonlandAndSuspendEffect suspendEffect =
                (ExileTopUntilNonlandAndSuspendEffect) effect;
        UUID controllerId = entry.getControllerId();
        List<Card> library = gameData.playerDecks.get(controllerId);
        String playerName = gameData.playerIdToName.get(controllerId);

        if (library == null || library.isEmpty()) {
            gameLogService.append(gameData,
                    GameLog.text(playerName + "'s library is empty (" + entry.getCard().getName() + ")."));
            return;
        }

        Card nonland = null;
        int exiledCount = 0;
        while (!library.isEmpty()) {
            Card topCard = library.removeFirst();
            exileService.exileCard(gameData, controllerId, topCard);
            exiledCount++;
            if (!topCard.hasType(CardType.LAND)) {
                nonland = topCard;
                break;
            }
        }

        if (nonland == null) {
            gameLogService.append(gameData, GameLog.text(
                    playerName + " exiles " + exiledCount + " card(s) from the top of their library"
                            + " — no nonland card found (" + entry.getCard().getName() + ")."));
            return;
        }

        gameData.exiledCardTimeCounters.put(nonland.getId(), suspendEffect.timeCounters());
        gameLogService.append(gameData, GameLog.builder()
                .text(playerName + " exiles cards until ").card(nonland)
                .text(" with " + suspendEffect.timeCounters() + " time counters and suspend ("
                        + entry.getCard().getName() + ").")
                .build());
        log.info("Game {} - {} exiled {} with {} suspend time counters",
                gameData.id, playerName, nonland.getName(), suspendEffect.timeCounters());
    }
}
