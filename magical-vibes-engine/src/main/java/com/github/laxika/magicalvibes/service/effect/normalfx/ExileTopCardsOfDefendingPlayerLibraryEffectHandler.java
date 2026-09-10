package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsOfDefendingPlayerLibraryEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ExileTopCardsOfDefendingPlayerLibraryEffectHandler implements NormalEffectHandlerBean {

    private final ExileService exileService;
    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTopCardsOfDefendingPlayerLibraryEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var exile = (ExileTopCardsOfDefendingPlayerLibraryEffect) effect;
        if (exile.count() <= 0
                || (entry.getTargetId() == null && entry.getAttackedTargetId() == null)) {
            return;
        }

        UUID defendingObjectId = entry.getTargetId() != null
                ? entry.getTargetId()
                : entry.getAttackedTargetId();
        UUID defendingPlayerId = gameData.playerIds.contains(defendingObjectId)
                ? defendingObjectId
                : gameQueryService.findPermanentController(gameData, defendingObjectId);
        if (defendingPlayerId == null) {
            return;
        }

        var library = gameData.playerDecks.get(defendingPlayerId);
        if (library == null || library.isEmpty()) {
            return;
        }

        int toExile = Math.min(exile.count(), library.size());
        for (int i = 0; i < toExile; i++) {
            Card card = library.removeFirst();
            exileService.exileCard(gameData, defendingPlayerId, card);
        }

        gameLogService.append(gameData, GameLog.text(
                gameData.playerIdToName.get(defendingPlayerId) + " exiles the top " + toExile
                        + " card" + (toExile == 1 ? "" : "s") + " of their library."));
    }
}
