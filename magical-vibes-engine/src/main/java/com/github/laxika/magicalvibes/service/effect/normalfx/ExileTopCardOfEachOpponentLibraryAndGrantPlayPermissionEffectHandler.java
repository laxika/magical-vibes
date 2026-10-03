package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardOfEachOpponentLibraryAndGrantPlayPermissionEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Resolves end-step abilities that exile qualifying opponents' top cards. */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExileTopCardOfEachOpponentLibraryAndGrantPlayPermissionEffectHandler
        implements NormalEffectHandlerBean {

    private final ExileService exileService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTopCardOfEachOpponentLibraryAndGrantPlayPermissionEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        if (controllerId == null) {
            return;
        }

        ExileTopCardOfEachOpponentLibraryAndGrantPlayPermissionEffect exileEffect =
                (ExileTopCardOfEachOpponentLibraryAndGrantPlayPermissionEffect) effect;
        String controllerName = gameData.playerIdToName.get(controllerId);
        for (UUID opponentId : gameData.orderedPlayerIds) {
            if (controllerId.equals(opponentId)) {
                continue;
            }
            if (gameData.playerPoisonCounters.getOrDefault(opponentId, 0)
                    < exileEffect.minimumPoisonCounters()) {
                continue;
            }

            List<Card> deck = gameData.playerDecks.get(opponentId);
            String opponentName = gameData.playerIdToName.get(opponentId);
            if (deck == null || deck.isEmpty()) {
                gameLogService.append(gameData, GameLog.text(
                        opponentName + "'s library is empty — nothing to exile."));
                continue;
            }

            Card topCard = deck.removeFirst();
            exileService.exileCard(gameData, opponentId, topCard);
            gameData.exilePlayPermissions.put(topCard.getId(), controllerId);
            gameData.exilePlayAnyManaTypeWhileExiled.add(topCard.getId());

            gameLogService.append(gameData, GameLog.builder()
                    .text(opponentName + " exiles ").card(topCard)
                    .text(" from the top of their library — " + controllerName
                            + " may play it for as long as it remains exiled.").build());
            log.info("Game {} - {} exiles {} from the top of their library; {} may play it while it remains exiled",
                    gameData.id, opponentName, topCard.getName(), controllerName);
        }
    }
}
