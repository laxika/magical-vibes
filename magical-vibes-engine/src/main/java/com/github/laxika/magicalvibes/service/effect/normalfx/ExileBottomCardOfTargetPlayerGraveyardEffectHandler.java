package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileBottomCardOfTargetPlayerGraveyardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExileBottomCardOfTargetPlayerGraveyardEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final ExileService exileService;
    private final GraveyardService graveyardService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileBottomCardOfTargetPlayerGraveyardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetPlayerId = entry.getTargetId();
        String targetPlayerName = gameData.playerIdToName.get(targetPlayerId);
        List<Card> graveyard = gameData.playerGraveyards.get(targetPlayerId);

        if (graveyard == null || graveyard.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(targetPlayerName + " has no cards in graveyard to exile."));
            log.info("Game {} - {} has no graveyard cards to exile", gameData.id, targetPlayerName);
            return;
        }

        Card bottomCard = graveyard.removeFirst();
        graveyardService.notifyCardsLeftGraveyard(gameData, targetPlayerId);
        exileService.exileCard(gameData, targetPlayerId, bottomCard);

        gameLogService.append(gameData,
                GameLog.textCardText(targetPlayerName + " exiles ", bottomCard, " from the bottom of their graveyard."));
        log.info("Game {} - {} exiles {} from the bottom of their graveyard",
                gameData.id, targetPlayerName, bottomCard.getName());
    }
}
