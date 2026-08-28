package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsOfEachOpponentEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves the Nightmare token's attack and block trigger. */
@Component
@RequiredArgsConstructor
public class ExileTopCardsOfEachOpponentEffectHandler implements NormalEffectHandlerBean {

    private final ExileService exileService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTopCardsOfEachOpponentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        int count = ((ExileTopCardsOfEachOpponentEffect) effect).count();
        if (count <= 0) {
            return;
        }

        UUID controllerId = entry.getControllerId();
        for (UUID opponentId : gameData.orderedPlayerIds) {
            if (opponentId.equals(controllerId)) {
                continue;
            }

            var library = gameData.playerDecks.get(opponentId);
            if (library == null || library.isEmpty()) {
                continue;
            }

            int toExile = Math.min(count, library.size());
            for (int i = 0; i < toExile; i++) {
                Card card = library.removeFirst();
                exileService.exileCard(gameData, opponentId, card);
            }

            gameLogService.append(gameData, GameLog.text(
                    gameData.playerIdToName.get(opponentId) + " exiles the top " + toExile
                            + " card" + (toExile == 1 ? "" : "s") + " of their library."));
        }
    }
}
