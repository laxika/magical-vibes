package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardOfOpponentLibraryControllerMayPlayThisTurnEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Knacksaw Clique: target opponent exiles the top card of their library; until end of turn the
 * source's controller may play that card. In a two-player game the single opponent is the only
 * legal target.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExileTopCardOfOpponentLibraryControllerMayPlayThisTurnEffectHandler implements NormalEffectHandlerBean {

    private final ExileService exileService;
    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTopCardOfOpponentLibraryControllerMayPlayThisTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();

        UUID opponentId = gameData.orderedPlayerIds.stream()
                .filter(id -> !id.equals(controllerId))
                .findFirst().orElse(null);
        if (opponentId == null) return;

        List<Card> deck = gameData.playerDecks.get(opponentId);
        String opponentName = gameData.playerIdToName.get(opponentId);
        if (deck == null || deck.isEmpty()) {
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(opponentName + "'s library is empty — nothing to exile."));
            return;
        }

        Card topCard = deck.removeFirst();
        exileService.exileCard(gameData, opponentId, topCard);

        // Controller may play the exiled card at normal costs/timing until end of turn.
        gameData.exilePlayPermissions.put(topCard.getId(), controllerId);
        gameData.exilePlayPermissionsExpireEndOfTurn.add(topCard.getId());

        String controllerName = gameData.playerIdToName.get(controllerId);
        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(opponentName + " exiles " + topCard.getName()
                + " from the top of their library — " + controllerName + " may play it this turn."));
        log.info("Game {} - {} exiles {} from {}'s library top; {} may play it this turn",
                gameData.id, opponentName, topCard.getName(), opponentName, controllerName);
    }
}
