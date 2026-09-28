package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileBottomCardOfTargetPlayerLibraryEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExileBottomCardOfTargetPlayerLibraryEffectHandler implements NormalEffectHandlerBean {

    private final ExileService exileService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileBottomCardOfTargetPlayerLibraryEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetPlayerId = entry.getTargetId();
        List<Card> deck = gameData.playerDecks.get(targetPlayerId);
        String targetName = gameData.playerIdToName.get(targetPlayerId);

        if (deck == null || deck.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(targetName + "'s library is empty — nothing to exile."));
            return;
        }

        Card bottomCard = deck.removeLast();
        exileService.exileCard(gameData, targetPlayerId, bottomCard);
        gameLogService.append(gameData, GameLog.textCardText(targetName + " exiles ", bottomCard,
                " from the bottom of their library."));
        log.info("Game {} - {} exiles {} from library bottom", gameData.id,
                targetName, bottomCard.getName());
    }
}
