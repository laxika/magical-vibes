package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.AnteRecipient;
import com.github.laxika.magicalvibes.model.effect.AnteTopCardEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** Resolves the mandatory ante of one or more players' top library cards. */
@Component
@RequiredArgsConstructor
@Slf4j
public class AnteTopCardEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AnteTopCardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        AnteTopCardEffect anteEffect = (AnteTopCardEffect) effect;
        if (anteEffect.recipient() == AnteRecipient.EACH_PLAYER) {
            for (UUID playerId : gameData.orderedPlayerIds) {
                anteTopCard(gameData, entry, playerId);
            }
            return;
        }

        anteTopCard(gameData, entry, entry.getControllerId());
    }

    private void anteTopCard(GameData gameData, StackEntry entry, UUID playerId) {
        List<Card> library = gameData.playerDecks.get(playerId);
        if (library == null || library.isEmpty()) {
            return;
        }

        Card anted = library.removeFirst();
        gameData.addToAnte(playerId, anted);
        gameLogService.append(gameData,
                GameLog.builder()
                        .text(gameData.playerIdToName.get(playerId) + " antes ")
                        .card(anted)
                        .text(". (")
                        .card(entry.getCard())
                        .text(")")
                        .build());
        log.info("Game {} - {} antes {} to {}", gameData.id,
                gameData.playerIdToName.get(playerId), anted.getName(), entry.getCard().getName());
    }
}
